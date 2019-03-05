package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public final class RoleBasedAccessRecord implements AttributeAccessDefinition {

    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String roleName;
    private final JsonPointer attribute;
    private final Set<Permission> permissions;

    @JdbiConstructor
    @SuppressWarnings("squid:S00107") // Having so many arguments seems reasonable solution here
    public RoleBasedAccessRecord(String serviceName,
                                 String resourceType,
                                 String resourceName,
                                 String roleName,
                                 String attribute,
                                 int permissions) {
        this(serviceName, resourceType, resourceName, roleName, JsonPointer.valueOf(attribute),
            Permissions.fromSumOf(permissions));
    }

    @Override
    public String getAttributeAsString() {
        return attribute.toString();
    }

    @Override
    public int getPermissionsAsInt() {
        return Permissions.sumOf(permissions);
    }
}
