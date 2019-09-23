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
import uk.gov.hmcts.reform.amapi.functional.FunctionalTestSuite;
import uk.gov.hmcts.reform.amapi.models.FilterResource;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.time.Instant;

import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter"})
public class FilterResourceApiTest extends FunctionalTestSuite {

    @Test
    public void verifyFilterResourceApi() {

        ExplicitAccessGrant explicitAccessGrant = createGenericExplicitAccessGrant();
        FilterResource filterResourceMetadata = FilterResource.builder()
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
                .data(JsonNodeFactory.instance.objectNode())
                .build())
            .attributeSecurityClassification(ImmutableMap.of(JsonPointer.valueOf(""), PUBLIC))
            .build();

        amApiClient.createResourceAccess(explicitAccessGrant).post(
            amApiClient.getAccessUrl() + "api/" + version + "/access-resource");

        Response response = amApiClient.filterResource(filterResourceMetadata)
            .post(amApiClient.getAccessUrl() + "api/" + version + "/filter-resource");

        response.then().statusCode(200);
        response.then().log();
    }
}
