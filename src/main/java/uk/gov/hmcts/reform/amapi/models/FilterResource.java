package uk.gov.hmcts.reform.amapi.models;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.Resource;

import java.util.Map;
import java.util.Set;

@Data
@Builder(builderClassName = "FilterResourceBuilder")
@JsonDeserialize(builder = FilterResource.FilterResourceBuilder.class)
public class FilterResource {
    private final String userId;
    private final Set<String> userRoles;
    private final Resource resource;
    private final Map<JsonPointer, SecurityClassification> attributeSecurityClassification;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FilterResourceBuilder {
        // Lombok will add constructor, setters, build method
    }

}
