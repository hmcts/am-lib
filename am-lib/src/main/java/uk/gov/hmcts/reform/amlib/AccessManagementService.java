package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.FilterService;
import uk.gov.hmcts.reform.amlib.internal.PermissionsService;
import uk.gov.hmcts.reform.amlib.internal.aspects.AuditLog;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.models.Role;
import uk.gov.hmcts.reform.amlib.internal.models.query.AttributeData;
import uk.gov.hmcts.reform.amlib.internal.repositories.AccessManagementRepository;
import uk.gov.hmcts.reform.amlib.internal.utils.SecurityClassifications;
import uk.gov.hmcts.reform.amlib.models.AccessEnvelope;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.models.RolePermissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;

@SuppressWarnings("PMD.ExcessiveImports")
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
    @AuditLog("explicit access revoked by '{{mdc:caller}}' to resource '{{accessMetadata.resourceId}}' defined as "
        + "'{{accessMetadata.resourceDefinition.serviceName}}|{{accessMetadata.resourceDefinition.resourceType}}|"
        + "{{accessMetadata.resourceDefinition.resourceName}}' from accessor '{{accessMetadata.accessorId}}' "
        + "with relationship '{{accessMetadata.relationship}}': {{accessMetadata.attribute}}")
    public void revokeResourceAccess(@NotNull @Valid ExplicitAccessMetadata accessMetadata) {
        jdbi.useExtension(AccessManagementRepository.class,
            dao -> dao.removeAccessManagementRecord(accessMetadata));
    }

    /**
     * Filters a list of {@link JsonNode} to remove fields that user has no access to (no READ permission) and returns
     * an envelope response consisting of id, filtered json and permissions for attributes.
     *
     * @param userId                           accessor ID
     * @param userRoles                        accessor roles
     * @param resources                        envelope {@link Resource} and corresponding metadata
     * @param attributeSecurityClassifications input security classification map from CCD
     * @return envelope list of {@link FilteredResourceEnvelope} with resource ID, filtered JSON and map of permissions
     * if access to resource is configured, otherwise null
     * @throws PersistenceException if any persistence errors were encountered
     */
    public List<FilteredResourceEnvelope> filterResource(@NotBlank String userId,
                                                         @NotEmpty Set<@NotBlank String> userRoles,
                                                         @NotNull List<@NotNull @Valid Resource> resources,
                                                         @NotEmpty @Valid Map<JsonPointer, SecurityClassification>
                                                             attributeSecurityClassifications) {
        return resources.stream()
            .map(resource -> filterResource(userId, userRoles, resource, attributeSecurityClassifications))
            .collect(toList());
    }

    /**
     * Filters {@link JsonNode} to remove fields that user has no access to (no READ permission). In addition to that
     * method also returns map of all permissions that user has to resource.
     *
     * @param userId                           accessor ID
     * @param userRoles                        accessor roles
     * @param resource                         envelope {@link Resource} and corresponding metadata
     * @param attributeSecurityClassifications input security classification map from CCD
     * @return envelope {@link FilteredResourceEnvelope} with resource ID, filtered JSON and map of permissions if
     * access to resource is configured, otherwise null.
     * @throws PersistenceException if any persistence errors were encountered,
     *                              or NoSuchElementException if root element missing
     */
    @AuditLog("filtered access to resource '{{resource.id}}' defined as '{{resource.definition.serviceName}}|"
        + "{{resource.definition.resourceType}}|{{resource.definition.resourceName}}' for accessor '{{userId}}' "
        + "in roles '{{userRoles}}' and SecurityClassification {{attributeSecurityClassifications}} : "
        + "{{result.access.accessType}} access with relationships {{result.relationships}} "
        + "and permissions {{result.access.permissions}} and user security classification "
        + "is {{result.userSecurityClassification}}")
    public FilteredResourceEnvelope filterResource(@NotBlank String userId,
                                                   @NotEmpty Set<@NotBlank String> userRoles,
                                                   @NotNull @Valid Resource resource,
                                                   @NotEmpty @Valid Map<@NotNull JsonPointer, SecurityClassification>
                                                       attributeSecurityClassifications) {

        //inThrows NoSuchElementException exception when root is missing
        if (isNull(attributeSecurityClassifications.get(JsonPointer.valueOf("")))) {
            throw new NoSuchElementException("Root element not found in input Security Classification");
        }

        List<ExplicitAccessRecord> explicitAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, resource.getId(), resource.getDefinition().getResourceType()));

        Map<JsonPointer, Set<Permission>> attributePermissions;
        AccessType accessType;

        if (explicitAccess.isEmpty()) {
            Set<String> filteredRoles = filterRolesWithExplicitAccessType(userRoles);

            if (requireNonNull(filteredRoles).isEmpty()) {
                return null;
            }

            attributePermissions = getPermissionsToResourceForRoles(resource.getDefinition(), filteredRoles);

            if (attributePermissions == null) {
                return null;
            }

            accessType = ROLE_BASED;

        } else {
            List<Map<JsonPointer, Set<Permission>>> permissionsForRelationships = explicitAccess.stream()
                .collect(collectingAndThen(groupingByWithNullKeys(ExplicitAccessRecord::getRelationship), Map::values))
                .stream()
                .map(explicitAccessRecords -> explicitAccessRecords.stream()
                    .collect(getMapCollector()))
                .collect(toList());

            attributePermissions = permissionsService.merge(permissionsForRelationships);
            accessType = EXPLICIT;
        }

        final Integer maxSecurityClassificationHierarchy = getMaxSecurityClassificationHierarchyForRoles(userRoles);

        SecurityClassification userSecurityClassification =
            SecurityClassification.fromHierarchy(maxSecurityClassificationHierarchy);

        Set<SecurityClassification> visibleSecurityClassificationsForUser = SecurityClassifications
            .getVisibleSecurityClassifications(maxSecurityClassificationHierarchy);

        JsonNode filteredJson = filterService.filterJson(resource.getData(), attributePermissions,
            attributeSecurityClassifications, visibleSecurityClassificationsForUser);

        Map<JsonPointer, Set<Permission>> visibleAttributePermissions =
            filterAttributePermissionsBySecurityClassification(attributePermissions,
                attributeSecurityClassifications, visibleSecurityClassificationsForUser);

        Set<String> relationships = explicitAccess.stream()
            .map(ExplicitAccessRecord::getRelationship).filter(rel -> rel != null)
            .collect(toSet());

        return FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resource.getId())
                .definition(resource.getDefinition())
                .data(filteredJson)
                .build())
            .userSecurityClassification(userSecurityClassification)
            .access(AccessEnvelope.builder()
                .permissions(visibleAttributePermissions)
                .accessType(accessType)
                .build())
            .relationships(relationships)
            .build();
    }

    /**
     * check group by when keys are null (eg. usage when relationship key for Annotation is null).
     *
     * @param classifier lamda function
     * @param <T>        Type of Input
     * @param <A>        Result output
     * @return
     */
    private static <T, A> Collector<T, ?, Map<A, List<T>>> groupingByWithNullKeys(
        Function<? super T, ? extends A> classifier) {
        return toMap(
            classifier,
            Collections::singletonList,
            (List<T> oldList, List<T> newCollection) -> {
                List<T> newList = new ArrayList<>(oldList.size() + 1);
                newList.addAll(oldList);
                newList.addAll(newCollection);
                return newList;
            });
    }

    private Map<JsonPointer, Set<Permission>> filterAttributePermissionsBySecurityClassification(
        Map<JsonPointer, Set<Permission>> attributePermissions, Map<JsonPointer, SecurityClassification>
        attributeSecurityClassifications, Set<SecurityClassification> userSecurityClassifications) {

        Map<JsonPointer, Set<Permission>> visibleAttributePermissions = new ConcurrentHashMap<>();

        attributePermissions.forEach((attribute, permissions) -> {
            SecurityClassification attributeSecurityClassification = attributeSecurityClassifications.get(attribute);

            // if no security classification, inherit from parent
            if (attributeSecurityClassification == null) {
                JsonPointer parentAttribute = attribute.head();
                while (attributeSecurityClassifications.get(parentAttribute) == null) {
                    parentAttribute = parentAttribute.head();
                    if (parentAttribute.toString().isEmpty()) {
                        break;
                    }
                }
                attributeSecurityClassification = attributeSecurityClassifications.get(parentAttribute);
            }

            // if sufficient security classification, add to map of visible attributes
            if (userSecurityClassifications.contains(attributeSecurityClassification)) {
                visibleAttributePermissions.put(attribute, permissions);
            }
        });
        return visibleAttributePermissions;
    }

    private Integer getMaxSecurityClassificationHierarchyForRoles(@NotEmpty Set<String> userRoles) {
        return jdbi.withExtension(AccessManagementRepository.class, dao ->
            dao.getRoles(userRoles, Stream.of(EXPLICIT, ROLE_BASED).collect(toSet())).stream()
                .mapToInt(role -> role.getSecurityClassification().getHierarchy())
                .max()
                .orElseThrow(NoSuchElementException::new));
    }

    private Set<String> filterRolesWithExplicitAccessType(Set<String> userRoles) {
        if (userRoles.isEmpty()) {
            return null;
        }

        return jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getRoles(userRoles, Collections.singleton(ROLE_BASED)).stream()
                .map(Role::getRoleName)
                .collect(toSet()));
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

    private Collector<AttributeAccessDefinition, ?, Map<JsonPointer, Set<Permission>>> getMapCollector() {
        return toMap(AttributeAccessDefinition::getAttribute, AttributeAccessDefinition::getPermissions);
    }
}
