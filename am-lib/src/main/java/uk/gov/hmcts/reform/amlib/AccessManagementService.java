package uk.gov.hmcts.reform.amlib;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.model.AccessManagement;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;

import java.util.List;

public class AccessManagementService {
    private Jdbi jdbi;
    private String msg = "Hello Dummy Service 2";

    public AccessManagementService(Jdbi jdbi) {
        this.jdbi = jdbi;

        this.jdbi.installPlugin(new SqlObjectPlugin());
    }

    public Integer getHello() {
        return jdbi.withExtension(AccessManagementRepository.class, dao -> dao.createAccessManagementRecord("resId", "accId"));

//        if (jdbi != null) {
//            List<Integer> ids = jdbi.withHandle(handle ->
//                    handle.createQuery("SELECT access_control_id FROM access_control")
//                            .mapTo(Integer.class)
//                            .list());
//            newMsg.append(' ');
//            newMsg.append(ids.size());
//        }

//        return "";
    }
}
