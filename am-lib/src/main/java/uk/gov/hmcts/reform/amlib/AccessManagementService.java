package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.FilterService;
import uk.gov.hmcts.reform.amlib.internal.PermissionsService;
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
     *
     * <p>Operation is performed in a transaction so that if not all records can be created then whole grant will fail.
     *
     * @param explicitAccessGrant an object that describes explicit access to resource
     */
    public void grantExplicitResourceAccess(@NotNull @Valid ExplicitAccessGrant explicitAccessGrant) {
        jdbi.useTransaction(handle -> {
            AccessManagementRepository dao = handle.attach(AccessManagementRepository.class);
            try {
                explicitAccessGrant.getAttributePermissions().entrySet().stream().map(attributePermission ->
                    ExplicitAccessRecord.builder()
                        .resourceId(explicitAccessGrant.getResourceId())
                        .accessorId(explicitAccessGrant.getAccessorId())
                        .permissions(attributePermission.getValue())
                        .accessType(explicitAccessGrant.getAccessType())
                        .serviceName(explicitAccessGrant.getServiceName())
                        .resourceType(explicitAccessGrant.getResourceType())
                        .resourceName(explicitAccessGrant.getResourceName())
                        .attribute(attributePermission.getKey())
                        .securityClassification(explicitAccessGrant.getSecurityClassification())
                        .build())
                    .forEach(dao::createAccessManagementRecord);
            } catch (Exception e) {
                throw new PersistenceException(e);
            }
        });
    }

    /**
     * Removes explicit access to resource accordingly to record configuration.
     *
     * <p>IMPORTANT: This is a cascade delete function and so if called on a specific attribute
     * it will remove specified attribute and all children attributes.
     *
     * @param explicitAccessMetadata an object to remove a specific explicit access record.
     */
    public void revokeResourceAccess(@NotNull @Valid ExplicitAccessMetadata explicitAccessMetadata) {
        jdbi.useExtension(AccessManagementRepository.class,
            dao -> dao.removeAccessManagementRecord(explicitAccessMetadata));
    }

    /**
     * Returns list of user ids who have access to resource or null if user has no access to this resource.
     *
     * @param userId     (accessorId)
     * @param resourceId resource Id
     * @return List of user ids (accessor id) or null
     */
    public List<String> getAccessorsList(String userId, String resourceId) {
        return jdbi.withExtension(AccessManagementRepository.class, dao -> {
            List<String> userIds = dao.getAccessorsList(userId, resourceId);

            return userIds.isEmpty() ? null : userIds;
        });
    }

    /**
     * Filters a list of {@link JsonNode} to remove fields that user has no access to (no READ permission) and returns
     * an envelope response consisting of resourceId, filtered json and permissions for attributes.
     *
     * @param userId    accessor ID
     * @param userRoles accessor roles
     * @param resources envelope {@link Resource} and corresponding metadata
     * @return envelope list of {@link FilterResourceResponse} with resource ID, filtered JSON and map of permissions
     *     if access to resource is configured, otherwise null.
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
     *     to resource is configured, otherwise null.
     */
    @SuppressWarnings("PMD") // AvoidLiteralsInIfCondition: magic number used until multiple roles are supported
    public FilterResourceResponse filterResource(@NotBlank String userId,
                                                 @NotEmpty Set<@NotBlank String> userRoles,
                                                 @NotNull @Valid Resource resource) {
        List<ExplicitAccessRecord> explicitAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, resource.getResourceId()));

        Map<JsonPointer, Set<Permission>> attributePermissions;

        if (explicitAccess.isEmpty()) {
            attributePermissions = getRolePermissions(resource.getType(), userRoles);

            if (attributePermissions == null) {
                return null;
            }

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

    private boolean explicitAccessType(String userRole) {
        return jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getRoleAccessType(userRole).equals(AccessType.EXPLICIT));
    }

    /**
     * Retrieves a list of {@link RoleBasedAccessRecord } and returns attribute and permissions values.
     *
     * @param resource  {@link ResourceDefinition} a unique service name, resource type and resource name
     * @param userRoles A set of user roles
     * @return a map of attributes and their corresponding permissions or null
     */
    public Map<JsonPointer, Set<Permission>> getRolePermissions(@NotNull @Valid ResourceDefinition resource,
                                                                @NotEmpty Set<@NotBlank String> userRoles) {
        List<Map<JsonPointer, Set<Permission>>> permissionsForRoles =
            jdbi.withExtension(AccessManagementRepository.class, dao ->
                userRoles.stream()
                    .map(role -> dao.getRolePermissions(resource, role))
                    .map(roleBasedAccessRecords -> roleBasedAccessRecords.stream()
                        .filter(record -> !explicitAccessType(record.getRoleName()))
                        .collect(getMapCollector()))
                    .collect(Collectors.toList()));

        if (permissionsForRoles.stream().allMatch(Map::isEmpty)) {
            return null;
        }

        return permissionsService.merge(permissionsForRoles);
    }

    private Collector<AttributeAccessDefinition, ?, Map<JsonPointer, Set<Permission>>> getMapCollector() {
        return Collectors.toMap(AttributeAccessDefinition::getAttribute, AttributeAccessDefinition::getPermissions);
    }
}
