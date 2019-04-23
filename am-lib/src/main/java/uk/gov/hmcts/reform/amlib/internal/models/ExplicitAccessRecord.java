package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
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

    @Override
    public String getAttributeAsString() {
        return getAttribute().toString();
    }

    @Override
    public int getPermissionsAsInt() {
        return Permissions.sumOf(permissions);
    }
}
