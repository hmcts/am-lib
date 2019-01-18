package uk.gov.hmcts.reform.amapi.functional;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class HealthCheckTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckTest.class);

    @Before
    public void before() {
        String appUrl = System.getenv("TEST_URL");
        if (appUrl == null) {
            appUrl = "http://localhost:2703";
        }

        RestAssured.baseURI = appUrl;
        RestAssured.useRelaxedHTTPSValidation();
        LOGGER.info("Base Url set to: " + RestAssured.baseURI);
    }

    @Test
    @Category(SmokeTest.class)
    public void healthcheck_returns_200() {
        get("/health")
            .then().statusCode(200)
            .and().body("status", equalTo("UP"));
        assertThat(2).isGreaterThan(1);

    }
}
