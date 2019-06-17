package uk.gov.hmcts.reform.amapi.functional;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestAssuredTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestAssuredTest.class);

    @BeforeEach
    public void before() {
        String appUrl = System.getenv("TEST_URL");
        if (appUrl == null) {
            appUrl = "http://localhost:3704";
        }

        RestAssured.baseURI = appUrl;
        RestAssured.useRelaxedHTTPSValidation();
        LOGGER.info("Base Url set to: " + RestAssured.baseURI);
    }
}
