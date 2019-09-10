package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = ResourceDefinition.ResourceDefinitionBuilder.class)
public final class ResourceDefinition {
    @NotBlank
    private final String serviceName;
    @NotBlank
    private final String resourceType;
    @NotBlank
    private final String resourceName;

    private final LocalDateTime lastUpdate;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResourceDefinitionBuilder {
        // Lombok will add constructor, setters, build method
    }
}
