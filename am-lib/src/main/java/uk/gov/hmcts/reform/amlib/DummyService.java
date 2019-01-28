package uk.gov.hmcts.reform.amlib;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.hmcts.reform.amlib.model.AccessManagement;
import uk.gov.hmcts.reform.amlib.repositories.AccessManagementRepository;

import java.util.List;

public class DummyService {
    private Jdbi jdbi;
    private String msg = "Hello Dummy Service 2";

    public DummyService(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public DummyService(String dbUrl, String user, String password) {
        if (dbUrl == null || user == null || password == null) {
            msg = "db url or user or pass is null";
        } else {
            jdbi = Jdbi.create(dbUrl, user, password);

            jdbi.installPlugin(new SqlObjectPlugin());
        }
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
