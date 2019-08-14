package uk.gov.hmcts.reform.amapi.functional;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.amapi.functional.client.AmApiClient;

import static org.hamcrest.core.IsEqual.equalTo;


@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
public class HealthCheckTest {

    public AmApiClient amApiClient;

    @Value("${TEST_URL:http://localhost:3704/}")
    protected String accessUrl;

    @Test
    @Tag("SmokeTest")
    public void healthcheckReturns200() {
        amApiClient = new AmApiClient(accessUrl);
        amApiClient.buildRequest().get(amApiClient.getAccessUrl() + "health")
            .then().statusCode(200)
            .and().body("status", equalTo("UP"));
    }

}
