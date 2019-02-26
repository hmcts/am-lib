package uk.gov.hmcts.reform.amlib.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DefaultPermission {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String attribute;
    private final String roleName;
    private final int permissions;
}
