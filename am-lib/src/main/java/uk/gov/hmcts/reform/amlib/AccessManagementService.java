package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;

public class AccessManagementService {
    private final Jdbi jdbi;

    public AccessManagementService(String url, String user, String password) {
        this.jdbi = Jdbi.create(url, user, password);

        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    public void createResourceAccess(String resourceId, String accessorId) {
        jdbi.useExtension(AccessManagementRepository.class,
            dao -> dao.createAccessManagementRecord(resourceId, accessorId));
    }

    /**
     * Returns `resourceJson` when record with userId and resourceId exist, otherwise null.
     * @param userId (accessorId)
     * @param resourceId resource id
     * @param resourceJson json
     * @return resourceJson or null
     */
    public JsonNode filterResource(String userId, String resourceId, JsonNode resourceJson) {
        boolean hasAccess = jdbi.withExtension(AccessManagementRepository.class,
            dao -> dao.explicitAccessExist(userId, resourceId));

        return hasAccess ? resourceJson : null;
    }
}
