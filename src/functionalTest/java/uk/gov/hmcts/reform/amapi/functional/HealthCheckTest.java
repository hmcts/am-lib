package uk.gov.hmcts.reform.amapi.functional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;
import static org.hamcrest.core.IsEqual.equalTo;

class HealthCheckTest extends RestAssuredTest {

    @Test
    @Tag("SmokeTest")
    void healthcheck_returns_200() {
        get("/health")
            .then().statusCode(200)
            .and().body("status", equalTo("UP"));
    }
}
