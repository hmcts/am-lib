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
import uk.gov.hmcts.reform.amlib.internal.aspects.AuditLog;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.repositories.AccessManagementRepository;
import uk.gov.hmcts.reform.amlib.models.AccessEnvelope;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

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
                        .accessorType(accessGrant.getAccessorType())
                        .serviceName(accessGrant.getServiceName())
                        .resourceType(accessGrant.getResourceType())
                        .resourceName(accessGrant.getResourceName())
                        .attribute(attributePermission.getKey())
                        .securityClassification(accessGrant.getSecurityClassification())
                        .relationship(accessGrant.getRelationship())
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
     * an envelope response consisting of id, filtered json and permissions for attributes.
     *
     * @param userId    accessor ID
     * @param userRoles accessor roles
     * @param resources envelope {@link Resource} and corresponding metadata
     * @return envelope list of {@link FilteredResourceEnvelope} with resource ID, filtered JSON and map of permissions
     *     if access to resource is configured, otherwise null
     * @throws PersistenceException if any persistence errors were encountered
     */
    public List<FilteredResourceEnvelope> filterResource(@NotBlank String userId,
                                                         @NotEmpty Set<@NotBlank String> userRoles,
                                                         @NotNull List<@NotNull @Valid Resource> resources) {
        return resources.stream()
            .map(resource -> filterResource(userId, userRoles, resource))
            .collect(toList());
    }

    /**
     * Filters {@link JsonNode} to remove fields that user has no access to (no READ permission). In addition to that
     * method also returns map of all permissions that user has to resource.
     *
     * @param userId    accessor ID
     * @param userRoles accessor roles
     * @param resource  envelope {@link Resource} and corresponding metadata
     * @return envelope {@link FilteredResourceEnvelope} with resource ID, filtered JSON and map of permissions if
     *     access to resource is configured, otherwise null.
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("filtered access to resource '{{resource.id}}' defined as '{{resource.definition.serviceName}}|"
        + "{{resource.definition.resourceType}}|{{resource.definition.resourceName}}' for accessor '{{userId}}' "
        + "in roles '{{userRoles}}': {{result.access.permissions}}")
    public FilteredResourceEnvelope filterResource(@NotBlank String userId,
                                                   @NotEmpty Set<@NotBlank String> userRoles,
                                                   @NotNull @Valid Resource resource) {
        List<ExplicitAccessRecord> explicitAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, resource.getId()));

        Map<JsonPointer, Set<Permission>> attributePermissions;
        AccessType accessType;

        if (explicitAccess.isEmpty()) {
            Set<String> filteredRoles = filterRolesWithExplicitAccessType(userRoles);

            if (Objects.requireNonNull(filteredRoles).isEmpty()) {
                return null;
            }

            attributePermissions = getPermissionsToResourceForRoles(resource.getDefinition(), filteredRoles);

            if (attributePermissions == null) {
                return null;
            }

            accessType = AccessType.ROLE_BASED;

        } else {
            List<Map<JsonPointer, Set<Permission>>> permissionsForRelationships = explicitAccess.stream()
                .collect(collectingAndThen(groupingBy(ExplicitAccessRecord::getRelationship), Map::values))
                .stream()
                .map(explicitAccessRecords -> explicitAccessRecords.stream()
                    .collect(getMapCollector()))
                .collect(toList());

            attributePermissions = permissionsService.merge(permissionsForRelationships);
            accessType = AccessType.EXPLICIT;
        }

        JsonNode filteredJson = filterService.filterJson(resource.getData(), attributePermissions);

        Set<String> relationships = explicitAccess.stream()
            .map(ExplicitAccessRecord::getRelationship)
            .collect(Collectors.toSet());

        return FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resource.getId())
                .definition(resource.getDefinition())
                .data(filteredJson)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(attributePermissions)
                .accessType(accessType)
                .build())
            .relationships(relationships)
            .build();
    }

    private Set<String> filterRolesWithExplicitAccessType(Set<String> userRoles) {
        if (userRoles.isEmpty()) {
            return null;
        }

        return jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getRoles(userRoles, AccessType.ROLE_BASED));
    }

    /**
     * Retrieves a list of {@link RoleBasedAccessRecord } and returns attribute and permissions values.
     *
     * @param resourceDefinition {@link ResourceDefinition} a unique service name, resource type and resource name
     * @param userRoles          set of user roles
     * @return a map of attributes and their corresponding permissions or null
     * @throws PersistenceException if any persistence errors were encountered
     */
    @AuditLog("returned role access to resource defined as '{{resourceDefinition.serviceName}}|"
        + "{{resourceDefinition.resourceType}}|{{resourceDefinition.resourceName}}' for roles '{{userRoles}}': "
        + "{{result}}")
    public Map<JsonPointer, Set<Permission>> getRolePermissions(@NotNull @Valid ResourceDefinition resourceDefinition,
                                                                @NotEmpty Set<@NotBlank String> userRoles) {
        // Delegation to shared private method from public service methods resolves auditing problem where
        // two audit logs would have been produced otherwise
        return getPermissionsToResourceForRoles(resourceDefinition, userRoles);
    }

    private Map<JsonPointer, Set<Permission>> getPermissionsToResourceForRoles(ResourceDefinition resourceDefinition,
                                                                               Set<String> userRoles) {
        List<Map<JsonPointer, Set<Permission>>> permissionsForRoles =
            jdbi.withExtension(AccessManagementRepository.class, dao -> userRoles.stream()
                .map(role -> dao.getRolePermissions(resourceDefinition, role))
                .map(roleBasedAccessRecords -> roleBasedAccessRecords.stream()
                    .collect(getMapCollector()))
                .collect(toList()));

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
