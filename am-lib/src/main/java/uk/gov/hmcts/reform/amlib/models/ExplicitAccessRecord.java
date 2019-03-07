package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ExplicitAccessRecord extends AbstractAccessMetadata implements AttributeAccessDefinition {

    private final Set<Permission> permissions;

    @Builder // All args constructor is needed for builder. @SuperBuilder cannot be used because IDE does not support it
    @SuppressWarnings("squid:S00107") // Having so many arguments seems reasonable solution here
    public ExplicitAccessRecord(String resourceId,
                                String accessorId,
                                String accessType,
                                String serviceName,
                                String resourceType,
                                String resourceName,
                                JsonPointer attribute,
                                Set<Permission> permissions,
                                String securityClassification) {
        super(resourceId, accessorId, accessType, serviceName, resourceType, resourceName, attribute,
            securityClassification);
        this.permissions = permissions;
    }

    @Override
    public String getAttributeAsString() {
        return getAttribute().toString();
    }

    @Override
    public int getPermissionsAsInt() {
        return Permissions.sumOf(permissions);
    }
}
