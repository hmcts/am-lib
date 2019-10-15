package uk.gov.hmcts.reform.amapi.functional.api;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.amapi.functional.FunctionalTestSuite;
import uk.gov.hmcts.reform.amapi.models.FilterResource;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.time.Instant;

import static uk.gov.hmcts.reform.amlib.enums.AccessorType.DEFAULT;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.ROLE;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
public class FilterResourceApiTest extends FunctionalTestSuite {

    String api =  "api/";
    String filterResouce = "/filter-resource";
    String resourceIdNullNotNull = "resourceId-null-notnull";
    String relationships = "relationships";
    String read = "READ";
    String update = "UPDATE";
    String firstElement = "access.permissions.values()[0][0]";

    @Test
    public void verifyFilterResourceApi() {

        ExplicitAccessGrant explicitAccessGrant = createGenericExplicitAccessGrant();

        FilterResource filterResourceMetadata = createGenericFilterResourceMetadata(null,
            null, null);
        amApiClient.createResourceAccess(explicitAccessGrant).post(
            amApiClient.getAccessUrl() + api + version + "/access-resource");

        Response response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + api + version + filterResouce);

        response.then().statusCode(200);
        response.then().log();
    }

    @Test
    public void verifyFilterRoleResourceWithNullRelationship() {

        //Null relationship
        createExplicitGrantForFilterCase(resourceId, "caseworker-test1", ROLE, null, READ);

        FilterResource filterResourceMetadata = createGenericFilterResourceMetadata(
            "caseworker-test1", resourceId, "caseworker-test1");
        Response response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + api + version + filterResouce);

        response.then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body(relationships, Matchers.hasSize(0))
            .body("resource.id", Matchers.equalTo(resourceId))
            .log();
    }

    @Test
    public void verifyFilterUserResourceWithNullRelationship() {

        //Null relationship
        createExplicitGrantForFilterCase(resourceId, accessorId, USER,null, READ);

        FilterResource filterResourceMetadata = createGenericFilterResourceMetadata(
            null,null, null);
        Response response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + api  + version + filterResouce);

        response.then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body(relationships, Matchers.hasSize(0))
            .body("resource.id", Matchers.equalTo(resourceId))
            .log();
    }

    @Test
    public void verifyFilterResourceMergingWithNullAndNotNullRelationships() {

        //Null relationship
        createExplicitGrantForFilterCase(
            resourceIdNullNotNull,"accessorIdNullNotNUll",USER, null, READ);

        //Notnull relationship
        createExplicitGrantForFilterCase(
            resourceIdNullNotNull,"accessorIdNullNotNUll",USER, relationship, UPDATE);

        FilterResource filterResourceMetadata = createGenericFilterResourceMetadata(
            "accessorIdNullNotNUll",resourceIdNullNotNull, relationship);
        Response response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + api  + version + filterResouce);

        response.then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body(relationships, Matchers.hasSize(1))
            .body(relationships, Matchers.contains("caseworker-test"))
            .body("access.permissions.values()[0].size()", Matchers.equalTo(2))
            .body("access.permissions.values()[0]", Matchers.containsInAnyOrder(read, update))
            .body("resource.id", Matchers.equalTo(resourceIdNullNotNull))
            .log();
    }

    @Test
    public void verifyFilterResourceWithWildCardPermission() {
        //Given When User wants Filter for Resource
        createExplicitGrantForFilterCase(resourceId, "*", DEFAULT, null, READ);

        //When I call Filter Resource API
        FilterResource filterResourceMetadata = createGenericFilterResourceMetadata(
            accessorId, resourceId, relationship);
        Response response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + api + version + filterResouce);

        //Then I can get Filter Envelope with wild card
        response.then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("resource.data.name", Matchers.equalTo("test"))
            .body(firstElement, Matchers.equalTo(read))
            .log();
    }

    @Test
    public void verifyFilterResourceWithWildCardAndExplicitPermission() {
        //Given When User wants Filter for Resource
        createExplicitGrantForFilterCase(resourceId, "*", DEFAULT, null, READ);
        createExplicitGrantForFilterCase(resourceId, accessorId, USER, relationship, UPDATE);

        //When I call Filter Resource API
        FilterResource filterResourceMetadata = createGenericFilterResourceMetadata(
            accessorId, resourceId, relationship);
        Response response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + api + version + filterResouce);

        //Then I can get Filter Envelope with wild card & Explicit permissions merged
        response.then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("resource.data.name", Matchers.equalTo("test"))
            .body("access.permissions.values()[0].size()", Matchers.equalTo(2))
            .body("access.permissions.values()[0]", Matchers.containsInAnyOrder(read, update))
            .log();
    }

    @Test
    public void verifyFilterResourceWhenWildCardPermissionRevoked() {
        //Given When User wants Filter wild card access to resource but wildcard access revoked
        createExplicitGrantForFilterCase(resourceId, "*", DEFAULT, null, READ);
        ExplicitAccessMetadata explicitAccessMetadata = ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId("*")
            .accessorType(DEFAULT)
            .attribute(attribute)
            .serviceName(serviceName)
            .resourceName(resourceName)
            .resourceType(resourceType)
            .build();
        amApiClient.revokeResourceAccess(explicitAccessMetadata);

        //When I call Get access Filter API
        FilterResource filterResourceMetadata = createGenericFilterResourceMetadata(
            accessorId, resourceId, relationship);
        Response response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + api + version + filterResouce);

        //Then I can get Filter Envelope should be empty
        response.then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .contentType(Matchers.isEmptyOrNullString())
            .log();
    }

    @Test
    public void verifyFilterResourceChecksBothResourceIdAndResourceType() {

        //GIVEN there is an explicit access record for a resource
        createExplicitGrantForFilterCase(resourceId, accessorId, accessorType, relationship, READ);
        ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(otherResourceType)
                .lastUpdate(Instant.now())
                .build())
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(accessorType)
            .relationship(relationship)
            .attributePermissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(UPDATE)))
            .lastUpdate(Instant.now())
            .build();

        //WHEN filterResource method or equivalent API is called
        FilterResource filterResourceMetadata = createGenericFilterResourceMetadata(
            accessorId, resourceId, relationship);
        Response response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + api + version + filterResouce);

        //THEN for the explicit access record used to provide access to the resource,
        // both the resourceId and resourceType must match, in addition to the accessor
        response.then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("resource.data.name", Matchers.equalTo("test"))
            .body("access.permissions.values()[0].size()", Matchers.equalTo(1))
            .body(firstElement, Matchers.equalTo(read))
            .log();
    }

}
