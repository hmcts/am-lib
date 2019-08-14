package uk.gov.hmcts.reform.amapi.functional;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;

import static org.hamcrest.core.IsEqual.equalTo;


@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
public class HealthCheckTest extends FunctionalTestSuite {

    @Test
    @Tag("SmokeTest")
    public void healthcheckReturns200() {
        amApiClient.buildRequest().get(amApiClient.getAccessUrl() + "health")
            .then().statusCode(200)
            .and().body("status", equalTo("UP"));
    }

}
