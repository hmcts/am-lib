package uk.gov.hmcts.reform.amlib;

import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class DummyService {
    private Jdbi jdbi;

    public DummyService(String dbUrl, String user, String password) {
        jdbi = Jdbi.create(dbUrl,user, password);
    }

    public String getHello() {

        List<Integer> ids = jdbi.withHandle(handle ->
                handle.createQuery("SELECT access_control_id FROM access_control")
                .mapTo(Integer.class)
                .list());

        return "Hello Dummy Service 2 " + ids.size();
    }
}
