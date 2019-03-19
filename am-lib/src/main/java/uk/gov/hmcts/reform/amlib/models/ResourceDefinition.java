package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public final class ResourceDefinition {
    @NotBlank
    private final String serviceName;
    @NotBlank
    private final String resourceType;
    @NotBlank
    private final String resourceName;
}
