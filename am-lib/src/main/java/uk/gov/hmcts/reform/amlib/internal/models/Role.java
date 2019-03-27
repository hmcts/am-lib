package uk.gov.hmcts.reform.amlib.internal.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

@Data
@Builder
@AllArgsConstructor
public final class Role {
    private final String roleName;
    private final RoleType roleType;
    private final SecurityClassification securityClassification;
    private final AccessManagementType accessManagementType;
}
