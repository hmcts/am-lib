package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;

import java.util.Set;

@Data
@Builder
public class AccessResourceEnvelope {
    private final String accessorId;
    private final AccessorType accessorType;
    private final Set<String> relationships;
}
