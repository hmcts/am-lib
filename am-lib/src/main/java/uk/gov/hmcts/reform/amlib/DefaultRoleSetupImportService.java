package uk.gov.hmcts.reform.amlib;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.aspects.AuditLog;
import uk.gov.hmcts.reform.amlib.internal.models.ResourceAttribute;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.repositories.DefaultRoleSetupRepository;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.reform.amlib.internal.aspects.AuditLog.Severity.DEBUG;

public class DefaultRoleSetupImportService {
    private final Jdbi jdbi;

    /**
     * This constructor has issues with performance due to requiring a new connection for every query.
     *
     * @param url      the url for the database
     * @param username the username for the database
     * @param password the password for the database
     */
    public DefaultRoleSetupImportService(String url, String username, String password) {
        this.jdbi = Jdbi.create(url, username, password)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * This constructor is recommended to be used over the above.
     *
     * @param dataSource the datasource for the database
     */
    public DefaultRoleSetupImportService(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource)
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
     * Creates a new resource resourceDefinition or does nothing if already exists.
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
                dao.createResourceAttribute(ResourceAttribute.builder()
                    .serviceName(accessGrant.getResourceDefinition().getServiceName())
                    .resourceName(accessGrant.getResourceDefinition().getResourceName())
                    .resourceType(accessGrant.getResourceDefinition().getResourceType())
                    .attribute(attribute)
                    .defaultSecurityClassification(permissionAndClassification.getValue())
                    .build()
                );

                dao.grantDefaultPermission(
                    RoleBasedAccessRecord.builder()
                        .serviceName(accessGrant.getResourceDefinition().getServiceName())
                        .resourceType(accessGrant.getResourceDefinition().getResourceType())
                        .resourceName(accessGrant.getResourceDefinition().getResourceName())
                        .attribute(attribute)
                        .roleName(accessGrant.getRoleName())
                        .permissions(permissionAndClassification.getKey())
                        .build());
            });
        });
    }

    /**
     * Deletes all default permissions within a service for a given resource type.
     *
     * <p>Operation uses a transaction and will rollback if any errors are encountered whilst adding entries.
     *
     * @param serviceName  the name of the service to delete default permissions for
     * @param resourceType the type of resource to delete default permissions for
     * @throws PersistenceException if any persistence errors were encountered causing transaction rollback
     */
    @AuditLog("default role access revoked by '{{mdc:caller}}' for service "
        + "defined as '{{serviceName}}|{{resourceType}}'")
    public void truncateDefaultPermissionsForService(@NotBlank String serviceName, @NotBlank String resourceType) {
        jdbi.useTransaction(handle -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
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
     * @throws PersistenceException if any persistence errors were encountered causing transaction rollback
     */
    @SuppressWarnings("LineLength")
    @AuditLog("default role access revoked by '{{mdc:caller}}' for resource defined as "
        + "'{{resourceDefinition.serviceName}}|{{resourceDefinition.resourceType}}|{{resourceDefinition.resourceName}}'")
    public void truncateDefaultPermissionsByResourceDefinition(@NotNull @Valid ResourceDefinition resourceDefinition) {
        jdbi.useTransaction(handle -> {
            DefaultRoleSetupRepository dao = handle.attach(DefaultRoleSetupRepository.class);
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
}
