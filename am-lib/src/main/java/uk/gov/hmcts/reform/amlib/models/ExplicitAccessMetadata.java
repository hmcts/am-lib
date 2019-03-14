package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public final class ExplicitAccessMetadata {
    @NotBlank
    private final String resourceId;
    @NotBlank
    private final String accessorId;
    @NotBlank
    private final String accessType;
    @NotBlank
    private final String serviceName;
    @NotBlank
    private final String resourceType;
    @NotBlank
    private final String resourceName;
    @NotNull
    private final JsonPointer attribute;
    @NotNull
    private final SecurityClassification securityClassification;

    public String getAttributeAsString() {
        return getAttribute().toString();
    }
}
