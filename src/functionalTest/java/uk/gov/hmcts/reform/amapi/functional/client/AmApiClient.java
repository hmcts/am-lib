package uk.gov.hmcts.reform.amapi.functional.client;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.restassured.specification.RequestSpecification;
import lombok.Data;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.reform.amapi.models.FilterResource;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

@Data
@SuppressWarnings({"PMD.LawOfDemeter"})
public class AmApiClient {

    private long currentDateTime  = System.currentTimeMillis();
    String resourceId = "resourceId" + currentDateTime;
    @NotEmpty Set<@NotBlank String> accessorIds;
    String accessorId = "accessorId" + currentDateTime;
    String resourceName = "claim-test";
    String resourceType = "case-test";
    String seriviceName = "cmc-test";
    String relationship = "caseworker-test";
    Set<String> userRoles = new HashSet<>();
    Map<JsonPointer, Set<Permission>> multipleAttributePermissions = ImmutableMap.of(
        JsonPointer.valueOf(""), ImmutableSet.of(READ, CREATE, UPDATE));

    private final String accessUrl;

    private final String s2sToken;

    private static final String SERVICE_HEADER = "ServiceAuthorization";

    public AmApiClient(String accessUrl,  String s2sToken) {
        this.accessUrl = accessUrl;
        this.s2sToken = s2sToken;
    }

    private RequestSpecification withUnauthenticatedRequest() {
        return SerenityRest.given()
            .relaxedHTTPSValidation()
            //.baseUri(professionalApiUrl)
            .header("Content-Type", APPLICATION_JSON_UTF8_VALUE)
            .header("Accepts", APPLICATION_JSON_UTF8_VALUE);
    }

    private RequestSpecification withAuthenticatedRequest() {
        return withUnauthenticatedRequest()
            .header(SERVICE_HEADER, "Bearer " + s2sToken);
    }

    public RequestSpecification createExplicitAccess() {
        accessorIds = new HashSet<>();
        accessorIds.add(accessorId);
        ExplicitAccessGrant requestBody = ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(accessorIds)
            .accessorType(USER)
            .resourceDefinition(ResourceDefinition.builder()
                .resourceName(resourceName).resourceType(resourceType).serviceName(seriviceName).build())
            .attributePermissions(multipleAttributePermissions)
            .relationship(relationship)
            .build();

        return withAuthenticatedRequest().body(requestBody);
    }

    public RequestSpecification createRevokeAccess(String accessorId, String resourceId) {
        ExplicitAccessMetadata requestBody = ExplicitAccessMetadata.builder()
            .accessorId(accessorId)
            .resourceId(resourceId)
            .resourceName(resourceName)
            .resourceType(resourceType)
            .serviceName(seriviceName)
            .accessorType(USER)
            .attribute(JsonPointer.valueOf(""))
            .relationship(relationship)
            .build();

        return withAuthenticatedRequest().body(requestBody);
    }

    public RequestSpecification createRevokeAccessWithoutOptionalParams(String accessorId, String resourceId) {
        ExplicitAccessMetadata requestBody = ExplicitAccessMetadata.builder()
            .accessorId(accessorId)
            .resourceId(resourceId)
            .resourceType(resourceType)
            .accessorType(USER)
            .attribute(JsonPointer.valueOf(""))
            .relationship(relationship)
            .build();

        return withAuthenticatedRequest().body(requestBody);
    }

    public RequestSpecification createFilterAccess(String resourceId, String accessorId) {
        JsonNode data = JsonNodeFactory.instance.objectNode().put("json", "resource");
        userRoles.add(relationship);
        FilterResource requestBody = FilterResource.builder()
            .userId(accessorId)
            .userRoles(userRoles)
            .resource(Resource.builder()
                .id(resourceId)
                .definition(ResourceDefinition.builder()
                    .resourceName(resourceName)
                    .resourceType(resourceType)
                    .serviceName(seriviceName)
                    .build())
                .data(data)
                .build())
            .attributeSecurityClassification(Collections.singletonMap(JsonPointer.valueOf(""),
                SecurityClassification.PUBLIC))
            .build();

        return withAuthenticatedRequest().body(requestBody);
    }
}
