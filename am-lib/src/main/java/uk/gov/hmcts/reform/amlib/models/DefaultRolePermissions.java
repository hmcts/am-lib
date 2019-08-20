package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.List;

@Data
@Builder
public class DefaultRolePermissions {
    private String role;
    private List<Permission> permissions;
}
