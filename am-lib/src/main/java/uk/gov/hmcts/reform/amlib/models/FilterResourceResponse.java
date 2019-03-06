package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilterResourceResponse {
    private String resourceId;
    private ResourceDefinition type;
    private JsonNode data;
    private Access access;
}
