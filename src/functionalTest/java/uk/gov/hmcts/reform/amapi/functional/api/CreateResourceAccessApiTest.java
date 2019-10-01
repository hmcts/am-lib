package uk.gov.hmcts.reform.amapi.functional.api;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.amapi.functional.FunctionalTestSuite;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.time.Instant;
import java.util.UUID;

import static com.microsoft.applicationinsights.web.dependencies.http.HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.DEFAULT;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.ROLE;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CreateResourceAccessApiTest extends FunctionalTestSuite {

    private final String expectedResourceDefinition = resourceDefinitionToString(serviceName,
        resourceName, resourceType);

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


        response.then()
            .assertThat()
            .statusCode(SC_UNSUPPORTED_MEDIA_TYPE);
        assertThat(response).isNotNull();
        response.then().log();
    }

    @Test
    public void verifyGrantExplicitAccessErrorScenariosWrongEndpoint() {

        ExplicitAccessGrant explicitAccessGrant = createGenericExplicitAccessGrant();

        Response response = amApiClient.createResourceAccess(explicitAccessGrant)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/access-resource-test");

        response.then()
            .assertThat()
            .statusCode(SC_NOT_FOUND);

        assertThat(response).isNotNull();
        response.then().log();
    }

    @Test
    public void verifyGrantExplicitAccessForRole() {

        resourceId = UUID.randomUUID().toString();
        ExplicitAccessGrant explicitAccessGrant = getExplicitAccessGrant(ROLE, relationship, accessorId);

        JsonPath responseBody = verifyResponseCreated(explicitAccessGrant);
        assertThat(responseBody.get("accessorIds").toString()).isEqualTo("[" + accessorId + "]");
        assertThat(responseBody.get("relationship").toString()).isEqualTo(relationship);
        assertThat(responseBody.get("attributePermissions").toString()).contains("READ");
        assertThat(responseBody.get("accessorType").toString()).isEqualTo(ROLE.toString());
        assertThat(responseBody.get("resourceId").toString()).isEqualTo(resourceId);
        assertThat(responseBody.get("resourceDefinition").toString()).isEqualTo(expectedResourceDefinition);
    }


    @Test
    public void verifyGrantExplicitAccessWithNullRelationship() {

        resourceId = UUID.randomUUID().toString();
        ExplicitAccessGrant explicitAccessGrant = getExplicitAccessGrant(USER, null, accessorId);

        JsonPath responseBody = verifyResponseCreated(explicitAccessGrant);
        assertThat(responseBody.get("accessorIds").toString()).isEqualTo("[" + accessorId + "]");
        assertNull(responseBody.get("relationship"),null);
        assertThat(responseBody.get("attributePermissions").toString()).contains("READ");
        assertThat(responseBody.get("accessorType").toString()).isEqualTo(accessorType.toString());
        assertThat(responseBody.get("resourceId").toString()).isEqualTo(resourceId);
        assertThat(responseBody.get("resourceDefinition").toString()).isEqualTo(expectedResourceDefinition);
    }

    @Test
    public void verifyGrantExplicitAccessForWildcard() {
        resourceId = UUID.randomUUID().toString();
        ExplicitAccessGrant explicitAccessGrant = getExplicitAccessGrant(DEFAULT, null, "*");
        JsonPath responseBody = verifyResponseCreated(explicitAccessGrant);
        assertThat(responseBody.get("accessorIds").toString()).isEqualTo("[*]");
        assertNull(responseBody.get("relationship"),null);
        assertThat(responseBody.get("attributePermissions").toString()).contains("READ");
        assertThat(responseBody.get("accessorType").toString()).isEqualTo(DEFAULT.toString());
        assertThat(responseBody.get("resourceId").toString()).isEqualTo(resourceId);
        assertThat(responseBody.get("resourceDefinition").toString()).isEqualTo(expectedResourceDefinition);
    }

    private ExplicitAccessGrant getExplicitAccessGrant(final AccessorType accessorType,
                                                       final String relationship, final String accessorId) {
        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .lastUpdate(Instant.now())
                .build())
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(accessorType)
            .relationship(relationship)
            .attributePermissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
            .lastUpdate(Instant.now())
            .build();
    }

    @NotNull
    private JsonPath verifyResponseCreated(ExplicitAccessGrant explicitAccessGrant) {
        Response response = amApiClient.createResourceAccess(explicitAccessGrant)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/access-resource");
        JsonPath responseBody = response.getBody().jsonPath();
        response.then().assertThat().statusCode(201).log();
        return responseBody;
    }

}
