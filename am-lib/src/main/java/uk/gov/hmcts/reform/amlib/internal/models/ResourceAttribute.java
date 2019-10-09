package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
//Class is only used for testing audit records validations
public final class ResourceAttribute {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final SecurityClassification defaultSecurityClassification;
    private Instant lastUpdate;
    @Setter
    private String callingServiceName;

    public String getAttributeAsString() {
        return attribute.toString();
    }
}
