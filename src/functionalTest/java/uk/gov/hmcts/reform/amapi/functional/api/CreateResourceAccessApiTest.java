package uk.gov.hmcts.reform.amapi.functional.api;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.amapi.functional.FunctionalTestSuite;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.models.AccessManagementAudit;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
public class CreateResourceAccessApiTest extends FunctionalTestSuite {

    private final String expectedResourceDefinition = resourceDefinitionToString(serviceName, resourceName, resourceType);

    @Test
    public void verifyCreateExplicitAccessApi() {

        ExplicitAccessGrant explicitAccessGrant = createGenericExplicitAccessGrant();

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

    @Test
    public void verifyGrantExplicitAccessForRole() {

        ExplicitAccessGrant explicitAccessGrant = ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .lastUpdate(Instant.now())
                .build())
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(AccessorType.ROLE)
            .attributePermissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
            .relationship(relationship)
            .accessManagementAudit(AccessManagementAudit.builder()
                .lastUpdate(Instant.now())
                .build())
            .build();

        Response response = amApiClient.createResourceAccess(explicitAccessGrant)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/access-resource");

        JsonPath responseBody = response.getBody().jsonPath();

        response.then().assertThat().statusCode(201).log();
        assertThat(responseBody.get("accessorIds").toString()).isEqualTo("[" + accessorId + "]");
        assertThat(responseBody.get("resourceId").toString()).isEqualTo(resourceId);
        assertThat(responseBody.get("resourceDefinition").toString()).isEqualTo(expectedResourceDefinition);
        assertThat(responseBody.get("relationship").toString()).isEqualTo(relationship);
        assertThat(responseBody.get("attributePermissions").toString()).contains("READ");
        assertThat(responseBody.get("accessorType").toString()).isEqualTo(AccessorType.ROLE.toString());
    }

    @Test
    public void verifyGrantExplicitAccessWithNullRelationship() {

        ExplicitAccessGrant explicitAccessGrant = ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .lastUpdate(Instant.now())
                .build())
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(accessorType)
            .attributePermissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
            .accessManagementAudit(AccessManagementAudit.builder()
                .lastUpdate(Instant.now())
                .build())
            .build();

        Response response = amApiClient.createResourceAccess(explicitAccessGrant)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/access-resource");

        JsonPath responseBody = response.getBody().jsonPath();

        response.then().assertThat().statusCode(201).log();
        assertThat(responseBody.get("accessorIds").toString()).isEqualTo("[" + accessorId + "]");
        assertThat(responseBody.get("resourceId").toString()).isEqualTo(resourceId);
        assertThat(responseBody.get("resourceDefinition").toString()).isEqualTo(expectedResourceDefinition);
        assertNull(responseBody.get("relationship"));
        assertThat(responseBody.get("attributePermissions").toString()).contains("READ");
        assertThat(responseBody.get("accessorType").toString()).isEqualTo(accessorType.toString());
    }
}
