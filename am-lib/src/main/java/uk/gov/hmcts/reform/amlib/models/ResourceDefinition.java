package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

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

    private LocalDateTime lastUpdate;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResourceDefinitionBuilder {
        // Lombok will add constructor, setters, build method
    }

    @JdbiConstructor
    public ResourceDefinition(final String serviceName, final String resourceType, final String resourceName) {
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }
}
