package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class FilteredResourceEnvelope {
    private String resourceId;
    private ResourceDefinition resourceDefinition;
    private JsonNode data;
    private AccessEnvelope access;
}
