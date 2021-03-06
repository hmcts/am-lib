package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonDeserialize(builder = Resource.ResourceBuilder.class)
public final class Resource {
    @NotBlank
    private final String id;
    @NotNull
    @Valid
    private final ResourceDefinition definition;
    @NotNull
    @ApiModelProperty(
        name = "data",
        example  = "{\"attribute\":\"string\"}",
        dataType = "string")
    private final JsonNode data;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResourceBuilder {
        // Lombok will add constructor, setters, build method
    }

}
