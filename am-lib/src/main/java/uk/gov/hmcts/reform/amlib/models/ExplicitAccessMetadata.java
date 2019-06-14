package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonDeserialize(builder = ExplicitAccessMetadata.ExplicitAccessMetadataBuilder.class)
public final class ExplicitAccessMetadata {
    @NotBlank
    private final String resourceId;
    @NotNull
    @Valid
    private final ResourceDefinition resourceDefinition;
    @NotBlank
    private final String accessorId;
    @NotNull
    private final AccessorType accessorType;
    @NotNull
    @ApiModelProperty(name = "attribute", value = "/attribute1",  dataType = "String")
    private final JsonPointer attribute;

    private final String relationship;

    @ApiModelProperty(hidden = true)
    public String getAttributeAsString() {
        return getAttribute().toString();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExplicitAccessMetadataBuilder {
        // Lombok will add constructor, setters, build method
    }
}
