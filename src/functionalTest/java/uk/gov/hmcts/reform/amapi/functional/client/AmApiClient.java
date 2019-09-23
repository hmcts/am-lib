package uk.gov.hmcts.reform.amapi.functional.client;

import io.restassured.specification.RequestSpecification;
import lombok.Data;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.reform.amapi.models.FilterResource;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Data
@SuppressWarnings({"PMD.LawOfDemeter"})
public class AmApiClient {

    /*private long currentDateTime  = System.currentTimeMillis();
    String resourceId = "resourceId" + currentDateTime;
    @NotEmpty Set<@NotBlank String> accessorIds;
    String accessorId = "accessorId" + currentDateTime;
    String resourceName = "claim-test";
    String resourceType = "case-test";
    String seriviceName = "cmc-test";
    String relationship = "caseworker-test";
    Set<String> userRoles = new HashSet<>();
    Map<JsonPointer, Set<Permission>> multipleAttributePermissions = ImmutableMap.of(
        JsonPointer.valueOf(""), ImmutableSet.of(READ, CREATE, UPDATE));*/

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
            .header("Content-Type", APPLICATION_JSON_UTF8_VALUE)
            .header("Accepts", APPLICATION_JSON_UTF8_VALUE);
    }

    private RequestSpecification withAuthenticatedRequest() {
        return withUnauthenticatedRequest()
            .header(SERVICE_HEADER, "Bearer " + s2sToken);
    }

    public RequestSpecification createResourceAccess(ExplicitAccessGrant explicitAccessGrant) {
        return withAuthenticatedRequest().body(explicitAccessGrant);
    }

    public RequestSpecification revokeResourceAccess(ExplicitAccessMetadata explicitAccessMetadata) {
        return withAuthenticatedRequest().body(explicitAccessMetadata);
    }

    public RequestSpecification filterResource(FilterResource filterResourceMetadata) {
        return withAuthenticatedRequest().body(filterResourceMetadata);
    }

    /*public RequestSpecification createExplicitAccess() {
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
    }*/
}
