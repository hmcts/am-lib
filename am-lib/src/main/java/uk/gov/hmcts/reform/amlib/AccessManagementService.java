package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;

public class AccessManagementService {
    private final Jdbi jdbi;

    public AccessManagementService(String url, String user, String password) {
        this.jdbi = Jdbi.create(url, user, password);

        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    /**
     * Grants explicit access to resource accordingly to record configuration.
     *
     * @param explicitAccessGrant a record that describes explicit access to resource
     */
    @Transaction
    public void grantExplicitResourceAccess(ExplicitAccessGrant explicitAccessGrant) {
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
            .forEach(explicitAccessRecord ->
                jdbi.useExtension(AccessManagementRepository.class,
                    dao -> dao.createAccessManagementRecord(explicitAccessRecord)));
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
     * Returns {@link FilterResourceResponse} when record with userId and resourceId exist and has READ permissions,
     * otherwise null.
     *
     * @param userId       (accessorId)
     * @param resourceId   resource id
     * @param resourceJson json
     * @return resourceJson or null
     */
    public FilterResourceResponse filterResource(String userId, String resourceId, JsonNode resourceJson) {
        ExplicitAccessRecord explicitAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, resourceId));

        if (explicitAccess == null) {
            return null;
        }

        if (READ.isGranted(explicitAccess.getPermissions())) {
            Map<JsonPointer, Set<Permission>> attributePermissions = new ConcurrentHashMap<>();
            attributePermissions.put(JsonPointer.valueOf("/"), Permissions.fromSumOf(explicitAccess.getPermissions()));

            return FilterResourceResponse.builder()
                .resourceId(resourceId)
                .data(resourceJson)
                .permissions(attributePermissions)
                .build();
        }

        return null;
    }
}
