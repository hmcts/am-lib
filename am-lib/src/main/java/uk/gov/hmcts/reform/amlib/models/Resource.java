package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class Resource {
    private final String resourceId;
    private final ResourceDefinition type;
    private final JsonNode resourceJson;
}
