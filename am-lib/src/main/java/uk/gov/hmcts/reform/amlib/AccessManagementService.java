package uk.gov.hmcts.reform.amlib;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;

import java.util.List;

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

    public List<String> checkAccess(UserDetails userDetails, String resourceId) {
        return jdbi.withExtension(AccessManagementRepository.class, dao ->  {
            List<String> userIds = dao.checkAccess(userDetails.getUserId(), resourceId);

            return (userIds.isEmpty()) ? null : userIds;
        });
    }
}
