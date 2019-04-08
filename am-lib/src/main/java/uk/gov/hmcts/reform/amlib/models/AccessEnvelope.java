package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public final class AccessEnvelope {
    private final Map<JsonPointer, Set<Permission>> permissions;
    private final AccessType accessType;
}
