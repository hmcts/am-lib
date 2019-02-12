package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;
import uk.gov.hmcts.reform.amlib.models.AccessManagement;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AccessManagementService {
    private final Jdbi jdbi;

    public AccessManagementService(String url, String user, String password) {
        this.jdbi = Jdbi.create(url, user, password);

        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    /**
     * Grants explicit access to resource accordingly to record configuration.
     *
     * @param explicitAccessRecord a record that describes explicit access to resource
     */
    public void createResourceAccess(ExplicitAccessRecord explicitAccessRecord) {
        jdbi.useExtension(AccessManagementRepository.class,
            dao -> dao.createAccessManagementRecord(explicitAccessRecord));
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
     * Returns FilterResourceResponse when record with userId and resourceId exist and has READ permissions,
     * otherwise null.
     *
     * @param userId       (accessorId)
     * @param resourceId   resource id
     * @param resourceJson json
     * @return resourceJson or null
     * @throws UnsupportedPermissionsException when permissions are above 31 or below 0.
     */
    public FilterResourceResponse filterResource(String userId, String resourceId, JsonNode resourceJson)
        throws UnsupportedPermissionsException {
        AccessManagement explicitAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, resourceId));

        if (explicitAccess == null) {
            return null;
        }

        if (Permission.hasPermissionTo(explicitAccess.getPermissions(), Permission.READ)) {
            Map<String, Set<Permission>> attributePermissions = new HashMap<>();
            attributePermissions.put("/", Permission.buildPermissions(explicitAccess.getPermissions()));

            return FilterResourceResponse.builder()
                .resourceId(resourceId)
                .data(resourceJson)
                .permissions(attributePermissions)
                .build();
        }

        return null;
    }
}
