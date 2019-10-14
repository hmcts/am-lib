package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.aspects.AuditLog;
import uk.gov.hmcts.reform.amlib.internal.models.ResourceAttribute;
import uk.gov.hmcts.reform.amlib.internal.models.ResourceAttributeAudit;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessAuditRecord;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.repositories.DefaultRoleSetupRepository;
import uk.gov.hmcts.reform.amlib.internal.utils.PropertyReader;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.DefaultRolePermissions;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.models.RolePermissionsForCaseTypeEnvelope;
import uk.gov.hmcts.reform.amlib.service.DefaultRoleSetupImportService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.amlib.internal.aspects.AuditLog.Severity.DEBUG;
import static uk.gov.hmcts.reform.amlib.internal.utils.PropertyReader.AUDIT_REQUIRED;

public class DefaultRoleSetupImportServiceImpl implements DefaultRoleSetupImportService {
    private final Jdbi jdbi;

    /**
     * This constructor has issues with performance due to requiring a new connection for every query.
     *
     * @param url      the url for the database
     * @param username the username for the database
     * @param password the password for the database
     */
    public DefaultRoleSetupImportServiceImpl(String url, String username, String password) {
        this.jdbi = Jdbi.create(url, username, password)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * This constructor is recommended to be used over the above.
     *
     * @param dataSource the datasource for the database
     */
    public DefaultRoleSetupImportServiceImpl(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * This constructor is recommended when you want to use existing transaction and not to span new transaction.
     *
     * @param transactionAwareDataSourceProxy TransactionAwareDataSourceProxy
     */
    public DefaultRoleSetupImportServiceImpl(TransactionAwareDataSourceProxy transactionAwareDataSourceProxy) {
        this.jdbi = Jdbi.create(transactionAwareDataSourceProxy)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * Creates a new unique service or updates description if already exists.
     *
     * @param serviceName the name of the service
     * @throws PersistenceException if any persistence errors were encountered
     */
    public void addService(@NotBlank String serviceName) {
        addService(serviceName, null);
    }

    /**
     * Creates a new unique service, with a description, or updates description if already exists.
     *
     * @param serviceName        the name of the service
     * @param serviceDescription a description of the service
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog(value = "added service '{{serviceName}}' described as '{{serviceDescription}}'", severity = DEBUG)
    public void addService(@NotBlank String serviceName, String serviceDescription) {
        jdbi.useExtension(DefaultRoleSetupRepository.class,
            dao -> dao.addService(serviceName, serviceDescription));
    }

    /**
     * Creates a new unique role or updates type, security classification and access type if already exists.
     *
     * @param roleName               the name of the role
     * @param roleType               the type of role
     * @param securityClassification the security classification for the role
     * @param accessType             the access type for the role
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog(value = "added role '{{roleName}}' of type '{{roleType}}/{{accessType}}'", severity = DEBUG)
    public void addRole(@NotBlank String roleName,
                        @NotNull RoleType roleType,
                        @NotNull SecurityClassification securityClassification,
                        @NotNull AccessType accessType) {
        jdbi.useExtension(DefaultRoleSetupRepository.class,
            dao -> dao.addRole(roleName, roleType, securityClassification, accessType));
    }

    /**
     * Creates a new resource definition or does nothing if already exists.
     *
     * @param resourceDefinition {@link ResourceDefinition} the definition for a resource
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog(value = "added resource defined as '{{resourceDefinition.serviceName}}"
        + "|{{resourceDefinition.resourceType}}|{{resourceDefinition.resourceName}}'", severity = DEBUG)
    public void addResourceDefinition(@NotNull @Valid ResourceDefinition resourceDefinition) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao ->
            dao.addResourceDefinition(resourceDefinition));
    }

    /**
     * Creates a new resource attribute with default permissions for a role or updates attributes if already exists.
     *
     * <p>Operation uses a transaction and will rollback if any errors are encountered whilst adding entries.
     *
     * @param accessGrant a container for granting default permissions
     * @throws PersistenceException if any persistence errors were encountered causing transaction rollback
     */
    @AuditLog("default role access granted by '{{mdc:caller}}' to resource "
        + "defined as '{{accessGrant.resourceDefinition.serviceName}}|{{accessGrant.resourceDefinition.resourceType}}|"
        + "{{accessGrant.resourceDefinition.resourceName}}' for role '{{accessGrant.roleName}}': "
        + "{{accessGrant.attributePermissions}}")
    public void grantDefaultPermission(@NotNull @Valid DefaultPermissionGrant accessGrant) {

        jdbi.useTransaction(handle -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
            accessGrant.getAttributePermissions().forEach((attribute, permissionAndClassification) -> {

                dao.createResourceAttribute(getResourceAttribute(accessGrant, attribute, permissionAndClassification));

                dao.grantDefaultPermission(getRoleAccess(accessGrant, attribute, permissionAndClassification));

                //check if Audit flag enabled & create Audit of attribute and permissions
                if (TRUE.toString().equalsIgnoreCase(PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
                    dao.createResourceAttributeForAudit(getResourceAttributeAudit(accessGrant, attribute,
                        permissionAndClassification));
                    dao.grantDefaultPermissionAudit(getRoleAccessAudit(accessGrant, attribute,
                        permissionAndClassification));
                }
            });
        });
    }

    private RoleBasedAccessRecord getRoleAccess(
        @NotNull @Valid DefaultPermissionGrant accessGrant,
        @NotNull JsonPointer attribute,
        Map.@NotNull Entry<@NotEmpty Set<@NotNull Permission>,
            @NotNull SecurityClassification> permissionAndClassification) {

        return RoleBasedAccessRecord.builder()
            .serviceName(accessGrant.getResourceDefinition().getServiceName())
            .resourceType(accessGrant.getResourceDefinition().getResourceType())
            .resourceName(accessGrant.getResourceDefinition().getResourceName())
            .attribute(attribute)
            .roleName(accessGrant.getRoleName())
            .permissions(permissionAndClassification.getKey())
            .callingServiceName(accessGrant.getCallingServiceName())
            .build();

    }

    private RoleBasedAccessAuditRecord getRoleAccessAudit(@NotNull @Valid DefaultPermissionGrant accessGrant,
                                                          @NotNull JsonPointer attribute,
                                                          Map.@NotNull Entry<@NotEmpty Set<@NotNull Permission>,
                                                              @NotNull SecurityClassification>
                                                              permissionAndClassification) {
        return RoleBasedAccessAuditRecord.builder()
            .serviceName(accessGrant.getResourceDefinition().getServiceName())
            .resourceType(accessGrant.getResourceDefinition().getResourceType())
            .resourceName(accessGrant.getResourceDefinition().getResourceName())
            .attribute(attribute)
            .roleName(accessGrant.getRoleName())
            .permissions(permissionAndClassification.getKey())
            .callingServiceName(accessGrant.getCallingServiceName())
            .changedBy(accessGrant.getChangedBy())
            .build();

    }

    private ResourceAttribute getResourceAttribute(@NotNull @Valid DefaultPermissionGrant accessGrant,
                                                   @NotNull JsonPointer attribute,
                                                   Map.@NotNull Entry<@NotEmpty Set<@NotNull Permission>,
                                                       @NotNull SecurityClassification> permissionAndClassification) {
        return ResourceAttribute.builder()
            .serviceName(accessGrant.getResourceDefinition().getServiceName())
            .resourceName(accessGrant.getResourceDefinition().getResourceName())
            .resourceType(accessGrant.getResourceDefinition().getResourceType())
            .attribute(attribute)
            .defaultSecurityClassification(permissionAndClassification.getValue())
            .callingServiceName(accessGrant.getCallingServiceName())
            .build();

    }

    private ResourceAttributeAudit getResourceAttributeAudit(@NotNull @Valid DefaultPermissionGrant accessGrant,
                                                             @NotNull JsonPointer attribute,
                                                             Map.@NotNull Entry<@NotEmpty Set<@NotNull Permission>,
                                                                 @NotNull SecurityClassification>
                                                                 permissionAndClassification) {
        return ResourceAttributeAudit.builder()
            .serviceName(accessGrant.getResourceDefinition().getServiceName())
            .resourceName(accessGrant.getResourceDefinition().getResourceName())
            .resourceType(accessGrant.getResourceDefinition().getResourceType())
            .attribute(attribute)
            .defaultSecurityClassification(permissionAndClassification.getValue())
            .callingServiceName(accessGrant.getCallingServiceName())
            .changedBy(accessGrant.getChangedBy())
            .build();

    }


    /**
     * Deletes all default permissions within a service for a given resource type.
     *
     * <p>Operation uses a transaction and will rollback if any errors are encountered whilst adding entries.
     *
     * @param serviceName        the name of the service to delete default permissions for
     * @param resourceType       the type of resource to delete default permissions for
     * @param callingServiceName calling service name for truncate
     * @param changedBy          changed by user
     * @throws PersistenceException if any persistence errors were encountered causing transaction rollback
     */
    @AuditLog("default role access revoked by '{{mdc:caller}}' for service "
        + "defined as '{{serviceName}}|{{resourceType}}'")
    public void truncateDefaultPermissionsForService(@NotBlank String serviceName, @NotBlank String resourceType,
                                                     String callingServiceName, String changedBy) {
        jdbi.useTransaction(handle -> {


            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);

            //check if Audit flag enabled & Inserts Audit attribute permissions and resource attributes
            if (TRUE.toString().equalsIgnoreCase(PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
                dao.revokeDefaultPermissionAudit(serviceName, resourceType, callingServiceName, changedBy);
                dao.revokeResourceAttributeAudit(serviceName, resourceType, callingServiceName, changedBy);
            }
            //Truncate
            dao.deleteDefaultPermissionsForRoles(serviceName, resourceType);
            dao.deleteResourceAttributes(serviceName, resourceType);


        });
    }

    /**
     * Deletes all default permissions within a service for a specific resource name and resource type.
     *
     * <p>Operation uses a transaction and will rollback if any errors are encountered whilst adding entries.
     *
     * @param resourceDefinition {@link ResourceDefinition} the definition of resource to delete default permissions for
     * @param callingServiceName calling service name for truncate
     * @param changedBy          changed by user
     * @throws PersistenceException if any persistence errors were encountered causing transaction rollback
     */
    @SuppressWarnings("LineLength")
    @AuditLog("default role access revoked by '{{mdc:caller}}' for resource defined as "
        + "'{{resourceDefinition.serviceName}}|{{resourceDefinition.resourceType}}|{{resourceDefinition.resourceName}}'"
    )
    public void truncateDefaultPermissionsByResourceDefinition(@NotNull @Valid ResourceDefinition resourceDefinition,
                                                               String callingServiceName, String changedBy) {
        jdbi.useTransaction(handle -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);

            //check if Audit flag enabled & Inserts Audit attribute permissions and resource attributes
            if (TRUE.toString().equalsIgnoreCase(PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
                dao.revokeDefaultPermissionAudit(resourceDefinition, callingServiceName, changedBy);
                dao.revokeResourceAttributeAudit(resourceDefinition, callingServiceName, changedBy);
            }

            dao.deleteDefaultPermissionsForRoles(resourceDefinition);
            dao.deleteResourceAttributes(resourceDefinition);
        });
    }

    /**
     * Deletes a resource definition.
     *
     * @param resourceDefinition {@link ResourceDefinition} the definition of resource to delete
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog(value = "deleted resource defined as '{{resourceDefinition.serviceName}}|"
        + "{{resourceDefinition.resourceType}}|{{resourceDefinition.resourceName}}'", severity = DEBUG)
    public void deleteResourceDefinition(@NotNull @Valid ResourceDefinition resourceDefinition) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao ->
            dao.deleteResourceDefinition(resourceDefinition));
    }

    /**
     * Deletes a role.
     *
     * @param roleName the role name to delete
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog(value = "deleted role '{{roleName}}'", severity = DEBUG)
    public void deleteRole(@NotBlank String roleName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao -> dao.deleteRole(roleName));
    }

    /**
     * Deletes a service.
     *
     * @param serviceName the service name to delete
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog(value = "deleted service '{{serviceName}}'", severity = DEBUG)
    public void deleteService(@NotBlank String serviceName) {
        jdbi.useExtension(DefaultRoleSetupRepository.class, dao -> dao.deleteService(serviceName));
    }

    @Override
    @AuditLog(value = "grantResourceDefaultPermissions for role '{{mapAccessGrant}}'")
    public void grantResourceDefaultPermissions(Map<@NotNull String, @NotEmpty
        List<@NotNull @Valid DefaultPermissionGrant>> mapAccessGrant) {

        final List<DefaultPermissionGrant> defaultPermissionGrants = mapAccessGrant.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());

        final List<ResourceDefinition> resourceDefinitions = defaultPermissionGrants.stream().map(
            defaultPermissionGrant -> defaultPermissionGrant.getResourceDefinition()).distinct()
            .collect(Collectors.toList());

        List<ResourceAttribute> resourceAttributeList = new ArrayList<>();
        List<RoleBasedAccessRecord> roleBasedAccessRecords = new ArrayList<>();

        defaultPermissionGrants.stream().forEach(accessGrant ->
            accessGrant.getAttributePermissions().forEach((attribute, permissionAndClassification) -> {
                resourceAttributeList.add(getResourceAttribute(accessGrant, attribute,
                    permissionAndClassification));
                roleBasedAccessRecords.add(getRoleAccess(accessGrant, attribute, permissionAndClassification));
            }));

        String callingServiceName = "";
        String changedBy = "";

        if (nonNull(defaultPermissionGrants) && defaultPermissionGrants.size() > 0) {
            callingServiceName = defaultPermissionGrants.get(0).getCallingServiceName() == null ? ""
                : defaultPermissionGrants.get(0).getCallingServiceName();
            changedBy = defaultPermissionGrants.get(0).getChangedBy() == null ? ""
                : defaultPermissionGrants.get(0).getChangedBy();
        }

        batchGrantPermissionAndResource(resourceDefinitions, resourceAttributeList, roleBasedAccessRecords,
            callingServiceName, changedBy);
    }

    private void batchGrantPermissionAndResource(List<ResourceDefinition> resourceDefinitions,
                                                 List<ResourceAttribute> resourceAttributes,
                                                 List<RoleBasedAccessRecord> roleBasedAccessRecords,
                                                 final String callingServiceName, final String changedBy) {


        jdbi.useTransaction(handle -> {

            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);

            //check if Audit flag enabled & batch audit with revoke for deleted records
            if (TRUE.toString().equalsIgnoreCase(PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
                dao.defaultPermissionAuditBatchRevoke(resourceDefinitions, callingServiceName, changedBy);

                dao.resourceAttributeAuditBatchRevoke(resourceDefinitions, callingServiceName, changedBy);
            }

            //Actual batch delete
            dao.deleteBatchDefaultPermissions(resourceDefinitions);
            dao.deleteBatchResourceAttributes(resourceAttributes);

            //Insert new batch records
            dao.createResourceAttributeBatch(resourceAttributes);
            dao.grantDefaultPermissionBatch(roleBasedAccessRecords);

            //Audit for newly inserted batch
            if (TRUE.toString().equalsIgnoreCase(PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
                dao.createResourceAttributeForAuditBatch(resourceAttributes, callingServiceName, changedBy);
                dao.grantDefaultPermissionAuditBatch(roleBasedAccessRecords, callingServiceName, changedBy);
            }

        });
    }

    /**
     * Returns the access control list for a specified case type.
     *
     * @param caseTypeIds a case type
     * @return a set of permissions RolePermissionsForCaseTypeEnvelope
     */
    public List<RolePermissionsForCaseTypeEnvelope> getRolePermissionsForCaseType(@NotEmpty List<String> caseTypeIds) {
        List<RolePermissionsForCaseTypeEnvelope> rolePermissionsForCaseType =
            jdbi.withExtension(DefaultRoleSetupRepository.class,
                dao -> dao.getRolePermissionsForMultipleCaseTypes(caseTypeIds));
        return rolePermissionsForCaseType;
    }

    /**
     * Returns the access control list for a specified case type.
     *
     * @param caseTypeId a case type
     * @return a set of permissions each role has for the specified case type
     */
    @AuditLog("returned role permissions for case type '{{caseTypeId}}': {{result}}")
    public RolePermissionsForCaseTypeEnvelope getRolePermissionsForCaseType(@NotBlank String caseTypeId) {
        List<DefaultRolePermissions> defaultRolePermissions = jdbi.withExtension(DefaultRoleSetupRepository.class,
            dao -> dao.getRolePermissionsForCaseType(caseTypeId));
        return RolePermissionsForCaseTypeEnvelope.builder()
            .caseTypeId(caseTypeId)
            .defaultRolePermissions(defaultRolePermissions)
            .build();
    }
}
