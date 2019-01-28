package uk.gov.hmcts.reform.amlib;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;

public class AccessManagementService {
    private Jdbi jdbi;

    public AccessManagementService(String url, String user, String password) {
        this.jdbi = jdbi.create(url, user, password);

        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    public void createResourceAccess(String resourceId, String accessorId) {
        jdbi.useExtension(AccessManagementRepository.class, dao -> dao.createAccessManagementRecord(resourceId, accessorId));
    }
}
