package uk.gov.hmcts.reform.amapi.functional.api;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.amapi.functional.FunctionalTestSuite;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
public class RevokeResourceAccessApiTest extends FunctionalTestSuite {

    @Test
    public void verifyRevokeExplicitAccessApi() {

        ExplicitAccessGrant explicitAccessGrant = createGenericExplicitAccessGrant();
        ExplicitAccessMetadata explicitAccessMetadata = ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(accessorType)
            .attribute(attribute)
            .relationship(relationship)
            .serviceName(serviceName)
            .resourceName(resourceName)
            .resourceType(resourceType)
            .build();

        Response response = createThenRevokeAccess(explicitAccessGrant, explicitAccessMetadata);
        response.then().statusCode(204);
        response.then().log();
    }

    @Test
    public void verifyRevokeExplicitAccessApiWithoutResourceNameAndServiceName() {

        ExplicitAccessGrant explicitAccessGrant = createGenericExplicitAccessGrant();
        ExplicitAccessMetadata explicitAccessMetadata = ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(accessorType)
            .attribute(attribute)
            .relationship(relationship)
            .resourceType(resourceType)
            .build();

        Response response = createThenRevokeAccess(explicitAccessGrant, explicitAccessMetadata);
        response.then().statusCode(204);
        response.then().log();
    }

    private Response createThenRevokeAccess(ExplicitAccessGrant explicitAccessGrant,
                                            ExplicitAccessMetadata explicitAccessMetadata) {
        amApiClient.createResourceAccess(explicitAccessGrant)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/access-resource");
        return amApiClient.revokeResourceAccess(explicitAccessMetadata)
            .delete(amApiClient.getAccessUrl() + "api/" + version + "/access-resource");
    }
}
