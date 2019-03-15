package uk.gov.hmcts.reform.amlib.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public final class ResourceDefinition {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
}
