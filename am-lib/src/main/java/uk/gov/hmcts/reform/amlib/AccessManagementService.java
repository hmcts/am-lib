package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.enums.Permissions;
import uk.gov.hmcts.reform.amlib.models.AccessManagement;
import uk.gov.hmcts.reform.amlib.models.ExplicitPermissions;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;

import java.util.List;

import java.util.Set;

public class AccessManagementService {
    private final Jdbi jdbi;

    public AccessManagementService(String url, String user, String password) {
        this.jdbi = Jdbi.create(url, user, password);

        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    /**
     * Returns void if method succeeds.
     * @param resourceId resource id
     * @param accessorId accessor id
     * @param explicitPermissions defines information about permissions given to the accessor
     */
    public void createResourceAccess(String resourceId, String accessorId, ExplicitPermissions explicitPermissions) {
        jdbi.useExtension(AccessManagementRepository.class,
            dao ->  {
                Set<Permissions> userPermissions = explicitPermissions.getUserPermissions();

                dao.createAccessManagementRecord(resourceId, accessorId, Permissions.sumOf(userPermissions));
            });
    }

    /**
     * Returns list of user ids who have access to resource or null if user has no access to this resource.
     * @param userId (accessorId)
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
     * Returns `resourceJson` when record with userId and resourceId exist and has READ permissions, otherwise null.
     * @param userId (accessorId)
     * @param resourceId resource id
     * @param resourceJson json
     * @return resourceJson or null
     */
    public JsonNode filterResource(String userId, String resourceId, JsonNode resourceJson) {
        AccessManagement explicitAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.getExplicitAccess(userId, resourceId));

        if (explicitAccess == null) {
            return null;
        }

        return Permissions.hasPermissionTo(explicitAccess.getPermissions(), Permissions.READ) ? resourceJson : null;
    }
}
