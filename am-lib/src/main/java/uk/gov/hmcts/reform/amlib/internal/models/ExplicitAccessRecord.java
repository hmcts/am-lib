package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import org.jdbi.v3.core.mapper.Nested;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.AccessManagementAudit;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;

import java.util.Set;

@Data
@Builder
//@AllArgsConstructor
@SuppressWarnings("PMD.ExcessiveParameterList")
public final class ExplicitAccessRecord implements AttributeAccessDefinition {
    private final String resourceId;
    private final String accessorId;
    private final AccessorType accessorType;
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final Set<Permission> permissions;
    private final String relationship;
    @Nested
    private final AccessManagementAudit accessManagementAudit;

    @Override
    public String getAttributeAsString() {
        return getAttribute().toString();
    }

    @Override
    public int getPermissionsAsInt() {
        return Permissions.sumOf(permissions);
    }


    public ExplicitAccessRecord(final String resourceId, final String accessorId, final AccessorType accessorType,
                                final String serviceName, final String resourceType, final String resourceName,
                                final JsonPointer attribute, final Set<Permission> permissions,
                                final String relationship, @Nested final AccessManagementAudit accessManagementAudit) {
        this.resourceId = resourceId;
        this.accessorId = accessorId;
        this.accessorType = accessorType;
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.attribute = attribute;
        this.permissions = permissions;
        this.relationship = relationship;
        this.accessManagementAudit = accessManagementAudit;
    }
}
