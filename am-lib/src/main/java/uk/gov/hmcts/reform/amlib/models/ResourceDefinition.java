package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.Instant;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = ResourceDefinition.ResourceDefinitionBuilder.class)
public final class ResourceDefinition {
    @NotBlank
    private String serviceName;
    @NotBlank
    private String resourceType;
    @NotBlank
    private String resourceName;

    @JsonIgnore
    private Instant lastUpdate;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResourceDefinitionBuilder {
        // Lombok will add constructor, setters, build method
    }

    //Used For @BeanMapper forIntegrationTesting non @JdbiConstructor params
    public ResourceDefinition() {
        super();
    }

    @JdbiConstructor
    public ResourceDefinition(final String serviceName, final String resourceType, final String resourceName) {
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }
}
