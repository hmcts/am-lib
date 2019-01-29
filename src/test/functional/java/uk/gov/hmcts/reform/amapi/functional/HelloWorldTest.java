package uk.gov.hmcts.reform.amapi.functional;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static io.restassured.RestAssured.get;

public class HelloWorldTest extends RestAssuredTest {

    @Test
    @Category(SmokeTest.class)
    public void helloWorld_returns_200() {
        get("/")
                .then().statusCode(200);
    }
}