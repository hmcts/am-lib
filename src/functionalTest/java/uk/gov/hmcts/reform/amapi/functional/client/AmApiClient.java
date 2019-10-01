package uk.gov.hmcts.reform.amapi.functional.client;

import io.restassured.response.Response;
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

    private final String accessUrl;

    private final String version;

    private final String s2sToken;

    private static final String SERVICE_HEADER = "ServiceAuthorization";

    public AmApiClient(String accessUrl, String version, String s2sToken) {
        this.accessUrl = accessUrl;
        this.version = version;
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

    public Response revokeResourceAccess(ExplicitAccessMetadata explicitAccessMetadata) {
        return withAuthenticatedRequest()
            .body(explicitAccessMetadata)
            .delete(accessUrl + "api/" + version + "/access-resource");
    }

    public RequestSpecification filterResource(FilterResource filterResourceMetadata) {
        return withAuthenticatedRequest().body(filterResourceMetadata);
    }
}
