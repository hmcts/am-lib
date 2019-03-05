package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.NonNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;
import uk.gov.hmcts.reform.amlib.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;


public class AccessManagementService {

    private final FilterService filterService = new FilterService();

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
    public void grantExplicitResourceAccess(ExplicitAccessGrant explicitAccessGrant) {
        if (explicitAccessGrant.getAttributePermissions().size() == 0) {
            throw new IllegalArgumentException("At least one attribute is required");
        }
        if (explicitAccessGrant.getAttributePermissions().entrySet().stream()
            .anyMatch(attributePermission -> attributePermission.getValue().isEmpty())) {
            throw new IllegalArgumentException("At least one permission per attribute is required");
        }

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
     * @param explicitAccessMetadata an object to remove a specific explicit access record.
     */
    public void revokeResourceAccess(ExplicitAccessMetadata explicitAccessMetadata) {
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
     * Filters {@link JsonNode} to remove fields that user has no access to (no READ permission). In addition to that
     * method also returns map of all permissions that user has to resource.
     *
     * @param userId       accessor ID
     * @param resourceId   resource ID
     * @param resourceJson JSON resource
     * @return envelope {@link FilterResourceResponse} with resource ID, filtered JSON and map of permissions if access
     *     to resource is configured, otherwise null.
     */
    public FilterResourceResponse filterResource(String userId, String resourceId, JsonNode resourceJson) {
        List<ExplicitAccessRecord> explicitAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, resourceId));

        if (explicitAccess.isEmpty()) {
            return null;
        }

        Map<JsonPointer, Set<Permission>> attributePermissions = explicitAccess.stream().collect(
            Collectors.toMap(AttributeAccessDefinition::getAttribute, AttributeAccessDefinition::getPermissions)
        );

        JsonNode filteredJson = filterService.filterJson(resourceJson, attributePermissions);

        return FilterResourceResponse.builder()
            .resourceId(resourceId)
            .data(filteredJson)
            .permissions(attributePermissions)
            .build();
    }

    /**
     * Retrieves a list of {@link RoleBasedAccessRecord } and returns attribute and permissions values.
     *
     * @param serviceName  name of service
     * @param resourceType type of resource
     * @param resourceName name of a resource
     * @param roleNames    A set of role names. Currently only one role name is supported but
     *                     in future implementations we shall support having multiple role names
     * @return a map of attributes and their corresponding permissions or null
     */
    @SuppressWarnings("PMD") // AvoidLiteralsInIfCondition: magic number used until multiple roles are supported
    public Map<JsonPointer, Set<Permission>> getRolePermissions(
        @NonNull String serviceName, @NonNull String resourceType,
        @NonNull String resourceName, @NonNull Set<String> roleNames) {
        if (roleNames.size() > 1) {
            throw new IllegalArgumentException("Currently a single role only is supported. "
                + "Future implementations will allow for multiple roles.");
        }

        List<RoleBasedAccessRecord> roleBasedAccessRecords = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getRolePermissions(serviceName, resourceType, resourceName, roleNames.iterator().next()));

        if (roleBasedAccessRecords.isEmpty()) {
            return null;
        }

        return roleBasedAccessRecords.stream().collect(
            Collectors.toMap(AttributeAccessDefinition::getAttribute, AttributeAccessDefinition::getPermissions)
        );
    }
}
