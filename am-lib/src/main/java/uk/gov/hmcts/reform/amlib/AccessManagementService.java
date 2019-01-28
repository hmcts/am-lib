package uk.gov.hmcts.reform.amlib;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.exceptions.AccessManagementException;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;

public class AccessManagementService {
    private Jdbi jdbi;

    public AccessManagementService(Jdbi jdbi) {
        this.jdbi = jdbi;

        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    public void createResourceAccess(String resourceId, String accessorId) throws AccessManagementException {
        Integer result;
        try {
            result = jdbi.withExtension(AccessManagementRepository.class, dao -> dao.createAccessManagementRecord(resourceId, accessorId));
        } catch (Exception e) {
            throw new AccessManagementException(e.getMessage());
        }

        if (result != 1) {
            throw new AccessManagementException();
        }
    }
}
