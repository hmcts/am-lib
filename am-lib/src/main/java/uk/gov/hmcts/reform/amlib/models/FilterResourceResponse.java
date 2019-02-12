package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public class FilterResourceResponse {
    private String resourceId;
    private JsonNode data;
    private Map<String, Set<Permission>> permissions;
}
