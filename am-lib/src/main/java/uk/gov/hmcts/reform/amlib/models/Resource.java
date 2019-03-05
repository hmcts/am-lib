package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Resource {
    private final String resourceId;
    private final ResourceType type;
    private final List<String> resourceRoles;
    private final JsonNode resourceJson;
}
