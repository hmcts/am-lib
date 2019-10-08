package uk.gov.hmcts.reform.amlib.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RolePermissionsForCaseTypeEnvelope {

    private String caseTypeId;
    private List<DefaultRolePermissions> defaultRolePermissions;

    public void addDefaultRolePermissions(DefaultRolePermissions defaultRolePermissions) {
        if (Objects.nonNull(this.getDefaultRolePermissions())) {
            this.getDefaultRolePermissions().add(defaultRolePermissions);
        } else {
            this.defaultRolePermissions = new ArrayList<>();
            this.defaultRolePermissions.add(defaultRolePermissions);
        }
    }
}
