package uk.gov.hmcts.reform.amapi.functional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.get;

@SuppressWarnings("PMD")
class HelloWorldTest extends RestAssuredTest {

    @Test
    @Tag("SmokeTest")
    void helloWorld_returns_200() {
        get("/")
                .then().statusCode(200);
    }
}