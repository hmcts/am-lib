package uk.gov.hmcts.reform.amlib;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.aspects.AuditLog;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.models.Role;
import uk.gov.hmcts.reform.amlib.internal.models.query.AttributeData;
import uk.gov.hmcts.reform.amlib.internal.repositories.AccessManagementRepository;
import uk.gov.hmcts.reform.amlib.internal.utils.SecurityClassifications;
import uk.gov.hmcts.reform.amlib.models.DefaultRolePermissions;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.models.RolePermissions;
import uk.gov.hmcts.reform.amlib.models.RolePermissionsForCaseTypeEnvelope;
import uk.gov.hmcts.reform.amlib.models.UserCaseRolesEnvelope;
import uk.gov.hmcts.reform.amlib.models.UserCasesEnvelope;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;
import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;

@SuppressWarnings("PMD.ExcessiveImports")
public class AccessManagementService {

    private final Jdbi jdbi;

    /**
     * This constructor has issues with performance due to requiring a new connection for every query.
     *
     * @param url      the url for the database
     * @param username the username for the database
     * @param password the password for the database
     */
    public AccessManagementService(String url, String username, String password) {
        this.jdbi = Jdbi.create(url, username, password)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * This constructor is recommended to be used over the above.
     *
     * @param dataSource the datasource for the database
     */
    public AccessManagementService(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * Grants explicit access to resource accordingly to record configuration.
     * Access can be granted to a user or multiple users for a resource.
     *
     * <p>Operation is performed in a transaction so that if not all records can be created then whole grant will fail.
     *
     * @param accessGrant an object that describes explicit access to resource
     * @throws PersistenceException if any persistence errors were encountered causing transaction rollback
     */
    @AuditLog("explicit access granted by '{{mdc:caller}}' to resource '{{accessGrant.resourceId}}' "
        + "defined as '{{accessGrant.resourceDefinition.serviceName}}|{{accessGrant.resourceDefinition.resourceType}}|"
        + "{{accessGrant.resourceDefinition.resourceName}}' for accessors '{{accessGrant.accessorIds}}' "
        + "with relationship '{{accessGrant.relationship}}': {{accessGrant.attributePermissions}}")
    public void grantExplicitResourceAccess(@NotNull @Valid ExplicitAccessGrant accessGrant) {
        jdbi.useTransaction(handle -> {
            AccessManagementRepository dao = handle.attach(AccessManagementRepository.class);
            accessGrant.getAccessorIds().forEach(accessorIds ->
                accessGrant.getAttributePermissions().entrySet().stream().map(attributePermission ->
                    ExplicitAccessRecord.builder()
                        .resourceId(accessGrant.getResourceId())
                        .accessorId(accessorIds)
                        .permissions(attributePermission.getValue())
                        .accessorType(accessGrant.getAccessorType())
                        .serviceName(accessGrant.getResourceDefinition().getServiceName())
                        .resourceType(accessGrant.getResourceDefinition().getResourceType())
                        .resourceName(accessGrant.getResourceDefinition().getResourceName())
                        .attribute(attributePermission.getKey())
                        .relationship(accessGrant.getRelationship())
                        .build())
                    .forEach(expAccessRecord -> {
                        if (nonNull(accessGrant.getRelationship())) {
                            dao.grantAccessManagementWithNotNullRelationship(expAccessRecord);
                        } else {
                            //Avoid duplicate insertion on Null relationship
                            dao.grantAccessManagementWithNullRelationship(expAccessRecord);
                        }
                    }));
        });
    }

    /**
     * Removes explicit access to resource accordingly to record configuration.
     *
     * <p>IMPORTANT: This is a cascade delete function and so if called on a specific attribute
     * it will remove specified attribute and all children attributes.
     *
     * @param accessMetadata an object to remove a specific explicit access record
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("explicit access revoked by '{{mdc:caller}}' to resource '{{accessMetadata.resourceId}}' defined as "
        + "'{{accessMetadata.serviceName}}|{{accessMetadata.resourceType}}|{{accessMetadata.resourceName}}' "
        + "from accessor '{{accessMetadata.accessorId}}' with relationship '{{accessMetadata.relationship}}': "
        + "{{accessMetadata.attribute}}")
    public void revokeResourceAccess(@NotNull @Valid ExplicitAccessMetadata accessMetadata) {
        jdbi.useExtension(AccessManagementRepository.class,
            dao -> dao.removeAccessManagementRecord(accessMetadata));
    }


    private Integer getMaxSecurityClassificationHierarchyForRoles(@NotEmpty Set<String> userRoles) {
        return jdbi.withExtension(AccessManagementRepository.class, dao ->
            dao.getRoles(userRoles, Stream.of(EXPLICIT, ROLE_BASED).collect(toSet())).stream()
                .mapToInt(role -> role.getSecurityClassification().getHierarchy())
                .max()
                .orElseThrow(NoSuchElementException::new));
    }


    /**
     * Retrieves {@link RolePermissions} filtered by role security classification for a specific resource and role.
     *
     * @param resourceDefinition {@link ResourceDefinition} a unique service name, resource type and resource name
     * @param roleName           user role name
     * @return {@link RolePermissions} a combination of permissions and security classifications for a role name
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("returned role access to resource defined as '{{resourceDefinition.serviceName}}|"
        + "{{resourceDefinition.resourceType}}|{{resourceDefinition.resourceName}}' for role '{{roleName}}': "
        + "permissions '{{result.permissions}}' from access type '{{result.roleAccessType}}' and "
        + "security classifications '{{result.securityClassifications}}' due to role security classification "
        + "'{{result.roleSecurityClassification}}'")
    public RolePermissions getRolePermissions(@NotNull @Valid ResourceDefinition resourceDefinition,
                                              @NotBlank String roleName) {
        Map<AccessType, SecurityClassification> roleData = jdbi.withExtension(AccessManagementRepository.class, dao ->
            dao.getRoles(Collections.singleton(roleName), Stream.of(EXPLICIT, ROLE_BASED).collect(toSet()))).stream()
            .collect(toMap(Role::getAccessType, Role::getSecurityClassification));

        if (roleData.isEmpty()) {
            return null;
        }

        SecurityClassification roleSecurityClassification = roleData.entrySet().iterator().next().getValue();

        List<AttributeData> attributeData = jdbi.withExtension(AccessManagementRepository.class, dao ->
            dao.getAttributeDataForResource(resourceDefinition, roleName,
                SecurityClassifications.getVisibleSecurityClassifications(roleSecurityClassification.getHierarchy())));

        if (attributeData.isEmpty()) {
            return null;
        }

        return RolePermissions.builder()
            .permissions(attributeData.stream()
                .collect(toMap(AttributeData::getAttribute, AttributeData::getPermissions)))
            .securityClassifications(attributeData.stream()
                .collect(toMap(AttributeData::getAttribute, AttributeData::getDefaultSecurityClassification)))
            .roleSecurityClassification(roleSecurityClassification)
            .roleAccessType(roleData.entrySet().iterator().next().getKey())
            .build();
    }

    /**
     * Retrieves a set of {@link ResourceDefinition} that user roles have root level create permissions for.
     *
     * @param userRoles a set of roles
     * @return a set of resource definitions
     */
    @SuppressWarnings("LineLength")
    @AuditLog("returned resources that user with roles '{{userRoles}}' has create permission to: {{result}}")
    public Set<ResourceDefinition> getResourceDefinitionsWithRootCreatePermission(@NotEmpty Set<@NotBlank String> userRoles) {
        Integer maxSecurityClassificationForRole = getMaxSecurityClassificationHierarchyForRoles(userRoles);

        return jdbi.withExtension(AccessManagementRepository.class, dao ->
            dao.getResourceDefinitionsWithRootCreatePermission(
                userRoles, SecurityClassifications.getVisibleSecurityClassifications(maxSecurityClassificationForRole)));
    }

    /**
     * Returns a list of roles that a user holds within a case.
     *
     * @param caseId a case id
     * @param userId a user id
     * @return a list of roles that the user holds within the case
     */
    @AuditLog("returned roles that user '{{userId}}' has within case '{{caseId}}': {{result}}")
    public UserCaseRolesEnvelope returnUserCaseRoles(@NotBlank String caseId, @NotBlank String userId) {
        List<String> roles = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getUserCaseRoles(caseId, userId));
        return UserCaseRolesEnvelope.builder()
            .caseId(caseId)
            .userId(userId)
            .roles(roles)
            .build();
    }

    /**
     * Returns a list of case ids that a given user has root level read permissions for.
     *
     * @param userId a user id
     * @return a list of case ids that the user has access to
     */
    @AuditLog("returned case ids that user with id '{{userId}}' has read permissions to: {{result}}")
    public UserCasesEnvelope returnUserCases(@NotBlank String userId) {
        List<String> cases = jdbi.withExtension(AccessManagementRepository.class, dao -> dao.getUserCases(userId));
        return UserCasesEnvelope.builder()
            .userId(userId)
            .cases(cases)
            .build();
    }

    public RolePermissionsForCaseTypeEnvelope returnRolePermissionsForCaseType(@NotBlank String caseTypeId) {
        List<DefaultRolePermissions> defaultRolePermissions = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getRolePermissionsForCaseType(caseTypeId));
        return RolePermissionsForCaseTypeEnvelope.builder()
            .caseTypeId(caseTypeId)
            .defaultRolePermissions(defaultRolePermissions)
            .build();
    }
}
