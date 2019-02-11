package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.List;

@Data
@Builder
public class FilterResourceResponse {
    private String resourceId;
    private JsonNode data;

    @JsonProperty("permissions")
    private List<Permission> permissions;
}
