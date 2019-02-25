package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.ErrorAddingEntriesToDatabaseException;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class AccessManagementService {

    private final FilterService filterService = new FilterService();

    private final Jdbi jdbi;

    public AccessManagementService(String url, String user, String password) {
        this.jdbi = Jdbi.create(url, user, password)
            .installPlugin(new SqlObjectPlugin());
    }

    /**
     * Grants explicit access to resource accordingly to record configuration.
     *
     * <p>Operation is performed in transaction so that if not all records can be created then whole grant will fail.
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
                        .explicitPermissions(attributePermission.getValue())
                        .accessType(explicitAccessGrant.getAccessType())
                        .serviceName(explicitAccessGrant.getServiceName())
                        .resourceType(explicitAccessGrant.getResourceType())
                        .resourceName(explicitAccessGrant.getResourceName())
                        .attribute(attributePermission.getKey().toString())
                        .securityClassification(explicitAccessGrant.getSecurityClassification())
                        .build())
                    .forEach(dao::createAccessManagementRecord);
            } catch (Exception e) {
                throw new ErrorAddingEntriesToDatabaseException(e);
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
     * @param userId  accessor ID
     * @param resourceId resource ID
     * @param resourceJson JSON resource
     * @return envelope {@link FilterResourceResponse} with resource ID, filtered JSON and map of permissions if access
     * to resource is configured, otherwise null.
     */
    public FilterResourceResponse filterResource(String userId, String resourceId, JsonNode resourceJson) {
        List<ExplicitAccessRecord> explicitAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, resourceId));

        if (explicitAccess.isEmpty()) {
            return null;
        }

        Map<JsonPointer, Set<Permission>> attributePermissions = explicitAccess.stream().collect(
            Collectors.toMap(
                explicitAccessRecord -> JsonPointer.valueOf(explicitAccessRecord.getAttribute()),
                explicitAccessRecord -> Permissions.fromSumOf(explicitAccessRecord.getPermissions())
            )
        );

        JsonNode filteredJson = filterService.filterJson(resourceJson, attributePermissions);

        return FilterResourceResponse.builder()
            .resourceId(resourceId)
            .data(filteredJson)
            .permissions(attributePermissions)
            .build();
    }
}
