package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ResourceAccessorsEnvelope {
    String resourceId;
    @JsonProperty("explicitAccess")
    List<ResourceAccessor> explicitAccessors;
}
