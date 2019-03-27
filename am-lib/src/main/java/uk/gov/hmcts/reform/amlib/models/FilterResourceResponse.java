package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public final class FilterResourceResponse {
    private String resourceId;
    private Set<String> relationships;
    private JsonNode data;
    private Map<JsonPointer, Set<Permission>> permissions;
}
