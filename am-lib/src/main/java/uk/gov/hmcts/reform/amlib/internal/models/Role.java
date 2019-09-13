package uk.gov.hmcts.reform.amlib.internal.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public final class Role {
    private final String roleName;
    private final RoleType roleType;
    private final SecurityClassification securityClassification;
    private final AccessType accessType;
    private Instant lastUpdate;
}
