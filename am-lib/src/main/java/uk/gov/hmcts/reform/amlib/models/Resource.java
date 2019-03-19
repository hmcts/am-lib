package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public final class Resource {
    @NotBlank
    private final String resourceId;
    @NotNull
    @Valid
    private final ResourceDefinition type;
    @NotNull
    private final JsonNode resourceJson;
}
