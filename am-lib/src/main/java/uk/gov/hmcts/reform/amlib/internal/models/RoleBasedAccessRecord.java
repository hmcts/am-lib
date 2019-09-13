package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import org.jdbi.v3.core.mapper.Nested;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.AccessManagementAudit;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;

import java.util.Set;

@Data
@Builder
//@AllArgsConstructor
public final class RoleBasedAccessRecord implements AttributeAccessDefinition {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String roleName;
    private final JsonPointer attribute;
    private final Set<Permission> permissions;

    @Nested
    private final AccessManagementAudit accessManagementAudit;

    @Override
    public String getAttributeAsString() {
        return attribute.toString();
    }

    @Override
    public int getPermissionsAsInt() {
        return Permissions.sumOf(permissions);
    }

    public RoleBasedAccessRecord(final String serviceName, final String resourceType,
                                 final String resourceName, final String roleName,
                                 final JsonPointer attribute, final Set<Permission> permissions,
                                 @Nested AccessManagementAudit accessManagementAudit) {
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.roleName = roleName;
        this.attribute = attribute;
        this.permissions = permissions;
        this.accessManagementAudit = accessManagementAudit;
    }
}
