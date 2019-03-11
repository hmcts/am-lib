package uk.gov.hmcts.reform.amlib.internal.repositories.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

@Data
@Builder
@AllArgsConstructor
public final class ResourceAttribute {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final SecurityClassification defaultSecurityClassification;

    public String getAttributeAsString() {
        return attribute.toString();
    }
}
