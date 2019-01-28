package integration.uk.gov.hmcts.reform.amlib;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class AccessManagementServiceIntegrationTest extends IntegrationBaseTest {

    @Test
    public void WhenCreatingResourceAccess_ResourceAccessAppearsInDatabase() {
        String resourceId = "asd";

        ams.createResourceAccess(resourceId, "dsa");

        String actualResourceId = jdbi.open().createQuery("select \"resourceId\" from \"AccessManagement\"")
                .mapTo(String.class)
                .findOnly();

        assertThat(resourceId).isEqualTo(actualResourceId);
    }

    @Test
    public void dummyTest() {
        ams.createResourceAccess("asd", "dsa");
    }
}
