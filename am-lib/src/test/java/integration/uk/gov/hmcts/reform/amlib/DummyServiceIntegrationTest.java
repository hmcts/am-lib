package integration.uk.gov.hmcts.reform.amlib;

import org.junit.Test;
import uk.gov.hmcts.reform.amlib.DummyService;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyServiceIntegrationTest extends DAOTest {

    @Test
    public void dummyTest() {
        jdbi.withHandle( handle ->
                handle.createUpdate("INSERT INTO access_control VALUES (2)").execute()
        );

        String msg = new DummyService(jdbi).getHello();

        assertThat(msg).isEqualTo("Hello Dummy Service 2 1");
    }
}
