package uk.gov.hmcts.reform.amapi.functional.api;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.amapi.functional.FunctionalTestSuite;
import uk.gov.hmcts.reform.amapi.models.FilterResource;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.time.Instant;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.ROLE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
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

        response.then()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .log();

        assertThatAccessRecordHasBeenRevoked();
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

        response.then()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .log();

        assertThatAccessRecordHasBeenRevoked();
    }

    @Test
    public void verifyRevokeExplicitAccessApiWithIncorrectResourceNameAndServiceName() {

        ExplicitAccessGrant explicitAccessGrant = createGenericExplicitAccessGrant();
        ExplicitAccessMetadata explicitAccessMetadata = ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(accessorType)
            .attribute(attribute)
            .relationship(relationship)
            .serviceName("some service")
            .resourceName("some resource")
            .resourceType(resourceType)
            .build();

        Response response = createThenRevokeAccess(explicitAccessGrant, explicitAccessMetadata);

        response.then()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .log();

        FilterResource filterResourceMetadata = createFilterResourceMetadata();

        response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/filter-resource");

        response.then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("resource.data.name", equalTo("John"))
            .log();
    }

    @Test
    public void verifyRevokeExplicitAccessApiForRole() {

        ExplicitAccessGrant explicitAccessGrant = ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .lastUpdate(Instant.now())
                .build())
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(ROLE)
            .attributePermissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
            .relationship(relationship)
            .lastUpdate(Instant.now())
            .build();
        ExplicitAccessMetadata explicitAccessMetadata = ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(ROLE)
            .attribute(attribute)
            .relationship(relationship)
            .serviceName(serviceName)
            .resourceName(resourceName)
            .resourceType(resourceType)
            .build();

        Response response = createThenRevokeAccess(explicitAccessGrant, explicitAccessMetadata);

        response.then()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .log();

        assertThatAccessRecordHasBeenRevoked();
    }

    @Test
    public void verifyRevokeExplicitAccessApiForRoleWithNullRelationship() {

        ExplicitAccessGrant explicitAccessGrant = ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .lastUpdate(Instant.now())
                .build())
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(ROLE)
            .attributePermissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
            .lastUpdate(Instant.now())
            .build();
        ExplicitAccessMetadata explicitAccessMetadata = ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(ROLE)
            .attribute(attribute)
            .serviceName(serviceName)
            .resourceName(resourceName)
            .resourceType(resourceType)
            .build();

        Response response = createThenRevokeAccess(explicitAccessGrant, explicitAccessMetadata);

        response.then()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .log();

        FilterResource.builder()
            .userId(accessorId)
            .userRoles(ImmutableSet.of())
            .resource(Resource.builder()
                .id(resourceId)
                .definition(ResourceDefinition.builder()
                    .serviceName(serviceName)
                    .resourceName(resourceName)
                    .resourceType(resourceType)
                    .lastUpdate(Instant.now())
                    .build())
                .data(JsonNodeFactory.instance.objectNode()
                    .put("name", "John"))
                .build())
            .attributeSecurityClassification(ImmutableMap.of(JsonPointer.valueOf(""), PUBLIC))
            .build();

        assertThatAccessRecordHasBeenRevoked();
    }

    private Response createThenRevokeAccess(ExplicitAccessGrant explicitAccessGrant,
                                            ExplicitAccessMetadata explicitAccessMetadata) {
        amApiClient.createResourceAccess(explicitAccessGrant)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/access-resource");
        return amApiClient.revokeResourceAccess(explicitAccessMetadata);
    }

    private FilterResource createFilterResourceMetadata() {
        return FilterResource.builder()
            .userId(accessorId)
            .userRoles(ImmutableSet.of(relationship))
            .resource(Resource.builder()
                .id(resourceId)
                .definition(ResourceDefinition.builder()
                    .serviceName(serviceName)
                    .resourceName(resourceName)
                    .resourceType(resourceType)
                    .lastUpdate(Instant.now())
                    .build())
                .data(JsonNodeFactory.instance.objectNode()
                    .put("name", "John"))
                .build())
            .attributeSecurityClassification(ImmutableMap.of(JsonPointer.valueOf(""), PUBLIC))
            .build();
    }

    private void assertThatAccessRecordHasBeenRevoked() {
        FilterResource filterResourceMetadata = createFilterResourceMetadata();

        Response response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/filter-resource");

        response.then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(isEmptyOrNullString())
            .log();
    }
}
