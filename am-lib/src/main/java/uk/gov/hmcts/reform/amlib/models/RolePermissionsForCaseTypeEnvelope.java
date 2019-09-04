package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RolePermissionsForCaseTypeEnvelope {
    private String caseTypeId;
    private List<DefaultRolePermissions> defaultRolePermissions;
}
