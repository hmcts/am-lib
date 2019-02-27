package uk.gov.hmcts.reform.amlib.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

@Data
@Builder
@AllArgsConstructor
public class ResourceAttribute {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String attribute;
    private final SecurityClassification securityClassification;
}
