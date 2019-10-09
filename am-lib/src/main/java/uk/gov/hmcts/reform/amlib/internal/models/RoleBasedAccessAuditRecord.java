package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.amlib.enums.AuditAction;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;

import java.time.Instant;
import java.util.Set;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
//Class is only used for testing audit records validations in Integration test cases
public class RoleBasedAccessAuditRecord implements AttributeAccessDefinition {

    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String roleName;
    private final JsonPointer attribute;
    private final Set<Permission> permissions;

    @Setter
    private String callingServiceName;

    @NotBlank
    private final Instant auditTimeStamp;

    @Setter
    private String changedBy;

    private final AuditAction action;

    @Override
    public String getAttributeAsString() {
        return attribute.toString();
    }

    @Override
    public int getPermissionsAsInt() {
        return Permissions.sumOf(permissions);
    }
}
