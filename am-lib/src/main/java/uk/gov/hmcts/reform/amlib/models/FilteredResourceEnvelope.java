package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Set;

@Data
@Builder
public final class FilteredResourceEnvelope {
    private final Resource resource;
    private final AccessEnvelope access;
    private final Set<String> relationships;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final SecurityClassification userSecurityClassification;
}
