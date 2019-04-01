package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.FilterService;
import uk.gov.hmcts.reform.amlib.internal.PermissionsService;
import uk.gov.hmcts.reform.amlib.internal.aspects.AuditLog;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.repositories.AccessManagementRepository;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class AccessManagementService {

    private final FilterService filterService = new FilterService();
    private final PermissionsService permissionsService = new PermissionsService();

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
        + "defined as '{{accessGrant.serviceName}}|{{accessGrant.resourceType}}|{{accessGrant.resourceName}}' "
        + "for accessors '{{accessGrant.accessorIds}}': {{accessGrant.attributePermissions}}")
    public void grantExplicitResourceAccess(@NotNull @Valid ExplicitAccessGrant accessGrant) {
        jdbi.useTransaction(handle -> {
            AccessManagementRepository dao = handle.attach(AccessManagementRepository.class);
            accessGrant.getAccessorIds().forEach(accessorIds ->
                accessGrant.getAttributePermissions().entrySet().stream().map(attributePermission ->
                    ExplicitAccessRecord.builder()
                        .resourceId(accessGrant.getResourceId())
                        .accessorId(accessorIds)
                        .permissions(attributePermission.getValue())
                        .accessType(accessGrant.getAccessType())
                        .serviceName(accessGrant.getServiceName())
                        .resourceType(accessGrant.getResourceType())
                        .resourceName(accessGrant.getResourceName())
                        .attribute(attributePermission.getKey())
                        .securityClassification(accessGrant.getSecurityClassification())
                        .build())
                    .forEach(dao::createAccessManagementRecord));
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
    @AuditLog("explicit access revoked by '{{mdc:caller}}' to resource '{{accessMetadata.resourceId}}' "
        + "defined as '{{accessMetadata.serviceName}}|{{accessMetadata.resourceType}}|{{accessMetadata.resourceName}}' "
        + "from accessor '{{accessMetadata.accessorId}}': {{accessMetadata.attribute}}")
    public void revokeResourceAccess(@NotNull @Valid ExplicitAccessMetadata accessMetadata) {
        jdbi.useExtension(AccessManagementRepository.class,
            dao -> dao.removeAccessManagementRecord(accessMetadata));
    }

    /**
     * Filters a list of {@link JsonNode} to remove fields that user has no access to (no READ permission) and returns
     * an envelope response consisting of resourceId, filtered json and permissions for attributes.
     *
     * @param userId    accessor ID
     * @param userRoles accessor roles
     * @param resources envelope {@link Resource} and corresponding metadata
     * @return envelope list of {@link FilterResourceResponse} with resource ID, filtered JSON and map of permissions
     *         if access to resource is configured, otherwise null
     * @throws PersistenceException if any persistence errors were encountered
     */
    public List<FilterResourceResponse> filterResource(@NotBlank String userId,
                                                       @NotEmpty Set<@NotBlank String> userRoles,
                                                       @NotNull List<@NotNull @Valid Resource> resources) {
        return resources.stream()
            .map(resource -> filterResource(userId, userRoles, resource))
            .collect(Collectors.toList());
    }

    /**
     * Filters {@link JsonNode} to remove fields that user has no access to (no READ permission). In addition to that
     * method also returns map of all permissions that user has to resource.
     *
     * @param userId    accessor ID
     * @param userRoles accessor roles
     * @param resource  envelope {@link Resource} and corresponding metadata
     * @return envelope {@link FilterResourceResponse} with resource ID, filtered JSON and map of permissions if access
     *         to resource is configured, otherwise null
     * @throws PersistenceException if any persistence errors were encountered
     */
    @SuppressWarnings("PMD") // AvoidLiteralsInIfCondition: magic number used until multiple roles are supported
    @AuditLog("filtered access to resource '{{resource.resourceId}}' "
        + "defined as '{{resource.type.serviceName}}|{{resource.type.resourceType}}|{{resource.type.resourceName}}' "
        + "for accessor '{{userId}}' in roles '{{userRoles}}': {{result.permissions}}")
    public FilterResourceResponse filterResource(@NotBlank String userId,
                                                 @NotEmpty Set<@NotBlank String> userRoles,
                                                 @NotNull @Valid Resource resource) {
        if (userRoles.size() > 1) {
            throw new IllegalArgumentException("Currently a single role only is supported. "
                + "Future implementations will allow for multiple roles.");
        }

        List<ExplicitAccessRecord> explicitAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, resource.getResourceId()));

        Map<JsonPointer, Set<Permission>> attributePermissions;

        if (explicitAccess.isEmpty()) {
            List<RoleBasedAccessRecord> roleBasedAccess = jdbi.withExtension(AccessManagementRepository.class,
                dao -> dao.getRolePermissions(
                    resource.getType().getServiceName(),
                    resource.getType().getResourceType(),
                    resource.getType().getResourceName(),
                    userRoles.iterator().next()));

            if (roleBasedAccess.isEmpty()) {
                return null;
            }

            if (explicitAccessType(userRoles)) {
                return null;
            }

            attributePermissions = roleBasedAccess.stream().collect(getMapCollector());

        } else {
            attributePermissions = explicitAccess.stream().collect(getMapCollector());
        }

        JsonNode filteredJson = filterService.filterJson(resource.getResourceJson(), attributePermissions);

        return FilterResourceResponse.builder()
            .resourceId(resource.getResourceId())
            .data(filteredJson)
            .permissions(attributePermissions)
            .build();
    }

    private boolean explicitAccessType(Set<String> userRoles) {
        return jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getRoleAccessType(userRoles.iterator().next())).equals(AccessManagementType.EXPLICIT);
    }

    /**
     * Retrieves a list of {@link RoleBasedAccessRecord } and returns attribute and permissions values.
     *
     * @param serviceName  name of service
     * @param resourceType type of resource
     * @param resourceName name of a resource
     * @param userRoles    A set of user roles
     * @return a map of attributes and their corresponding permissions or null
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("returned role access to resource defined as '{{serviceName}}|{{resourceType}}|{{resourceName}}' "
        + "for roles '{{userRoles}}': {{result}}")
    public Map<JsonPointer, Set<Permission>> getRolePermissions(@NotBlank String serviceName,
                                                                @NotBlank String resourceType,
                                                                @NotBlank String resourceName,
                                                                @NotEmpty Set<@NotBlank String> userRoles) {
        List<Map<JsonPointer, Set<Permission>>> permissionsForRoles =
            jdbi.withExtension(AccessManagementRepository.class, dao ->
                userRoles.stream()
                    .map(role -> dao.getRolePermissions(serviceName, resourceType, resourceName, role))
                    .map(roleBasedAccessRecords -> roleBasedAccessRecords.stream()
                        .collect(getMapCollector()))
                    .collect(Collectors.toList()));

        if (permissionsForRoles.stream().allMatch(Map::isEmpty)) {
            return null;
        }

        return permissionsService.merge(permissionsForRoles);
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
        return jdbi.withExtension(AccessManagementRepository.class, dao ->
            dao.getResourceDefinitionsWithRootCreatePermission(userRoles));
    }

    private Collector<AttributeAccessDefinition, ?, Map<JsonPointer, Set<Permission>>> getMapCollector() {
        return Collectors.toMap(AttributeAccessDefinition::getAttribute, AttributeAccessDefinition::getPermissions);
    }
}
