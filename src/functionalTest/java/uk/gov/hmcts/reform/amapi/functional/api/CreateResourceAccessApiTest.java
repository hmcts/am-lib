package uk.gov.hmcts.reform.amapi.functional.api;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.amapi.functional.FunctionalTestSuite;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
public class CreateResourceAccessApiTest extends FunctionalTestSuite {

    @Test
    public void verifyCreateExplicitAccessApi() {

        ExplicitAccessGrant explicitAccessGrant = createGenericExplicitAccessGrant();
        String expectedResourceDefinition = resourceDefinitionToString(serviceName, resourceName, resourceType);

        Response response = amApiClient.createResourceAccess(explicitAccessGrant)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/access-resource");

        response.then().statusCode(201);
        response.then().log();
        JsonPath responseBody = response.getBody().jsonPath();
        assertThat(responseBody.get("accessorIds").toString()).isEqualTo("[" + accessorId + "]");
        assertThat(responseBody.get("resourceId").toString()).isEqualTo(resourceId);
        assertThat(responseBody.get("resourceDefinition").toString()).isEqualTo(expectedResourceDefinition);
        assertThat(responseBody.get("relationship").toString()).isEqualTo(relationship);
        assertThat(responseBody.get("attributePermissions").toString()).contains("READ");
        assertThat(responseBody.get("accessorType").toString()).isEqualTo(accessorType.toString());
        response.then().log();
    }

    @Test
    public void verifyGrantExplicitAccessErrorScenariosInvalidMediaType() {

        ExplicitAccessGrant explicitAccessGrant = createGenericExplicitAccessGrant();

        Response response = amApiClient.createResourceAccess(explicitAccessGrant)
            .header("Content-Type", "application/xml")
            .post(amApiClient.getAccessUrl() + "api/" + version + "/access-resource");

        response.then().statusCode(415);
        response.then().log();
    }

    @Test
    public void verifyGrantExplicitAccessErrorScenariosWrongEndpoint() {

        ExplicitAccessGrant explicitAccessGrant = createGenericExplicitAccessGrant();

        Response response = amApiClient.createResourceAccess(explicitAccessGrant)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/access-resource-test");

        response.then().statusCode(404);
        response.then().log();
    }
}
