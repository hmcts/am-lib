package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.FilterService;
import uk.gov.hmcts.reform.amlib.internal.PermissionsService;
import uk.gov.hmcts.reform.amlib.internal.aspects.AuditLog;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.models.Role;
import uk.gov.hmcts.reform.amlib.internal.repositories.AccessManagementRepository;
import uk.gov.hmcts.reform.amlib.internal.utils.SecurityClassifications;
import uk.gov.hmcts.reform.amlib.internal.validation.ValidAttributeSecurityClassification;
import uk.gov.hmcts.reform.amlib.models.AccessEnvelope;
import uk.gov.hmcts.reform.amlib.models.AccessResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.AccessorListByResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Stream;
import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;

@SuppressWarnings("PMD.ExcessiveImports")
public class FilterResourceService {

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
    public FilterResourceService(String url, String username, String password) {
        this.jdbi = Jdbi.create(url, username, password)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * This constructor is recommended to be used over the above.
     *
     * @param dataSource the datasource for the database
     */
    public FilterResourceService(DataSource dataSource) {
        this.jdbi = Jdbi.create(dataSource)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * Filters a list of {@link JsonNode} to remove fields that user has no access to (no READ permission or
     * insufficient security classification) and returns an envelope response consisting of id, filtered json
     * and permissions for attributes.
     *
     * @param userId                           accessor ID
     * @param userRoles                        accessor roles
     * @param resources                        envelope {@link Resource} and corresponding metadata
     * @param attributeSecurityClassifications input security classification map from CCD
     * @return envelope list of {@link FilteredResourceEnvelope} with resource ID, filtered JSON and map of permissions
     * if access to resource is configured, otherwise null
     * @throws PersistenceException if any persistence errors were encountered
     */
    public List<FilteredResourceEnvelope> filterResources(@NotBlank String userId,
                                                          @NotEmpty Set<@NotBlank String> userRoles,
                                                          @NotNull List<@NotNull @Valid Resource> resources,
                                                          Map<JsonPointer, SecurityClassification>
                                                              attributeSecurityClassifications) {
        return resources.stream()
            .map(resource -> filterResource(userId, userRoles, resource, attributeSecurityClassifications))
            .collect(toList());
    }

    /**
     * Filters {@link JsonNode} to remove fields that user has no access to (no READ permission or insufficient
     * security classification). In addition to that method also returns map of all permissions that user has to
     * resource.
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
                                                   @ValidAttributeSecurityClassification
                                                       Map<@NotNull JsonPointer, SecurityClassification>
                                                       attributeSecurityClassifications) {

        List<ExplicitAccessRecord> explicitAccessRecords = getExplicitAccessRecords(userId, userRoles, resource);

        Map<JsonPointer, Set<Permission>> attributePermissions;
        AccessType accessType;
        Set<String> relationships = Collections.emptySet();

        if (explicitAccessRecords.isEmpty()) {
            attributePermissions = getRoleAttributePermissions(userRoles, resource);
            accessType = ROLE_BASED;
        } else {
            attributePermissions = getExplicitAttributePermissions(explicitAccessRecords);
            accessType = EXPLICIT;
            relationships = getRelationshipsFromExplicitAccessRecords(explicitAccessRecords);
        }

        if (attributePermissions == null) {
            return null;
        }

        JsonNode filteredJson;
        Map<JsonPointer, Set<Permission>> visibleAttributePermissions;
        SecurityClassification userSecurityClassification = null;

        if (attributeSecurityClassifications == null) {
            filteredJson = filterService.filterJson(resource.getData(), attributePermissions);
            visibleAttributePermissions = attributePermissions;

        } else {
            Integer maxSecurityClassificationHierarchy = getMaxSecurityClassificationHierarchyForRoles(userRoles);
            Set<SecurityClassification> visibleSecurityClassificationsForUser = SecurityClassifications
                .getVisibleSecurityClassifications(maxSecurityClassificationHierarchy);

            userSecurityClassification = SecurityClassification.fromHierarchy(maxSecurityClassificationHierarchy);
            filteredJson = filterService.filterJson(resource.getData(), attributePermissions,
                attributeSecurityClassifications, visibleSecurityClassificationsForUser);
            visibleAttributePermissions = filterAttributePermissionsBySecurityClassification(
                attributePermissions, attributeSecurityClassifications, visibleSecurityClassificationsForUser);
        }

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
     * returns Access rights for given resource.
     *
     * @param resourceId   resourceId
     * @param resourceName resourceName
     * @param resourceType resourceType
     * @return AccessorListByResourceEnvelope AccessResourceEnvelope
     */
    @AuditLog("returns access rights for given resource with resource id '{{resourceId}}' "
        + "  resource name '{{resourceName}} and resource type '{{resourceType}}' : with result resource id "
        + " {{result.resourceId}} and data {{result.explicitAccessResourceEnvelopesList}}")
    public AccessorListByResourceEnvelope returnResourceAccessList(@NotBlank String resourceId,
                                           @NotBlank String resourceName, @NotBlank String resourceType) {

        //collect root-level attribute
        List<ExplicitAccessRecord> explicitAccessRecords = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccessForResource(resourceId, resourceName, resourceType, AccessorType.USER));

        Map<String, List<ExplicitAccessRecord>> explicitAccessRecordsMap = explicitAccessRecords.stream()
            .collect(groupingBy(ExplicitAccessRecord::getAccessorId));

        final List<AccessResourceEnvelope> accessResourceEnvelopes = new ArrayList<>();

        explicitAccessRecordsMap.entrySet().forEach(explicitAccessRecord -> {
            //set permissions
            Map<JsonPointer, Set<Permission>> permissions = getExplicitAttributePermissions(
                explicitAccessRecord.getValue());
            //set relationships
            Set<String> relationships = getRelationshipsFromExplicitAccessRecords(explicitAccessRecord.getValue());
            AccessEnvelope accessEnvelope = AccessEnvelope.builder().permissions(permissions).build();
            accessResourceEnvelopes.add(AccessResourceEnvelope.builder()
                .accessorId(explicitAccessRecord.getKey())
                .access(accessEnvelope).accessorType(AccessorType.USER)
                .relationships(relationships).build());
        });

        return AccessorListByResourceEnvelope.builder()
            .explicitAccessResourceEnvelopesList(accessResourceEnvelopes)
            .resourceId(resourceId).build();
    }

    private Integer getMaxSecurityClassificationHierarchyForRoles(@NotEmpty Set<String> userRoles) {
        return jdbi.withExtension(AccessManagementRepository.class, dao ->
            dao.getRoles(userRoles, Stream.of(EXPLICIT, ROLE_BASED).collect(toSet())).stream()
                .mapToInt(role -> role.getSecurityClassification().getHierarchy())
                .max()
                .orElseThrow(NoSuchElementException::new));
    }


    private List<ExplicitAccessRecord> getExplicitAccessRecords(String userId,
                                                                @NotEmpty Set<String> userRoles,
                                                                Resource resource) {
        return jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, userRoles, resource.getId(),
                resource.getDefinition().getResourceType()));
    }

    private Map<JsonPointer, Set<Permission>> getRoleAttributePermissions(Set<String> userRoles,
                                                                          Resource resource) {
        Set<String> filteredRoles = filterRolesWithExplicitAccessType(userRoles);
        Map<JsonPointer, Set<Permission>> attributePermissions = null;
        if (!filteredRoles.isEmpty()) {
            attributePermissions = getPermissionsToResourceForRoles(resource.getDefinition(), filteredRoles);
        }
        return attributePermissions;
    }

    private Map<JsonPointer, Set<Permission>> getExplicitAttributePermissions(
        List<ExplicitAccessRecord> explicitAccessRecords) {

        // group records by accessor / relationship combo
        Map<String, List<ExplicitAccessRecord>> recordsByAccessorIdAndRelationship = explicitAccessRecords.stream()
            .collect(groupingBy(ear -> ear.getAccessorId() + "." + ear.getRelationship()));

        // create map of attribute permissions for each accessor / relationship combo
        List<Map<JsonPointer, Set<Permission>>> permissionsByAccessorIdAndRelationship = new ArrayList<>();
        recordsByAccessorIdAndRelationship.forEach((accessorId, records) -> {
            Map<JsonPointer, Set<Permission>> permissions = new ConcurrentHashMap<>();
            records.forEach(record -> permissions.put(record.getAttribute(), record.getPermissions()));
            permissionsByAccessorIdAndRelationship.add(permissions);
        });

        // merge attribute permissions between each accessor / relationship combo
        return permissionsService.merge(permissionsByAccessorIdAndRelationship);
    }

    private Set<String> getRelationshipsFromExplicitAccessRecords(List<ExplicitAccessRecord> explicitAccessRecords) {
        return explicitAccessRecords.stream()
            .map(ExplicitAccessRecord::getRelationship)
            .filter(Objects::nonNull)
            .collect(toSet());
    }

    private Set<String> filterRolesWithExplicitAccessType(Set<String> userRoles) {
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

    private Collector<AttributeAccessDefinition, ?, Map<JsonPointer, Set<Permission>>> getMapCollector() {
        return toMap(AttributeAccessDefinition::getAttribute, AttributeAccessDefinition::getPermissions);
    }
}
