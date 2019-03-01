package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public final class RoleBasedAccessRecord {

    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String attribute;
    private final String roleName;
    private final int permissions;

    public JsonPointer getAttribute() {
        return JsonPointer.valueOf(attribute);
    }

    public Set<Permission> getPermissions() {
        return Permissions.fromSumOf(permissions);
    }
}
