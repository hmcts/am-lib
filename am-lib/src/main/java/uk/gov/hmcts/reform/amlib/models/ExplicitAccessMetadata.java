package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

@Data
@Builder
public final class ExplicitAccessMetadata {
    private final String resourceId;
    private final String accessorId;
    private final String accessType;
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final SecurityClassification securityClassification;

    public String getAttributeAsString() {
        return getAttribute().toString();
    }
}
