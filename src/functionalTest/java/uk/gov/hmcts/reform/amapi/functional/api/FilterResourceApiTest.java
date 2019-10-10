package uk.gov.hmcts.reform.amapi.functional.api;

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
            .body("access.permissions.values()[0][0]", Matchers.equalTo("UPDATE"))
            .body("access.permissions.values()[0][1]", Matchers.equalTo("READ"))
            .body("resource.id", Matchers.equalTo(resourceIdNullNotNull))
            .log();
    }

}
