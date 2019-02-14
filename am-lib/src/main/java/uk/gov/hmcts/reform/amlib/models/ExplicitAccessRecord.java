package uk.gov.hmcts.reform.amlib.models;

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
public class ExplicitAccessRecord {

    private final String resourceId;
    private final String accessorId;
    private final Set<Permission> explicitPermissions;
    private final String accessType;
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String attribute;
    private final String securityClassification;

    @JdbiConstructor
    @SuppressWarnings("squid:S00107") // Having so many arguments seems reasonable solution here
    public ExplicitAccessRecord(String resourceId,
                                String accessorId,
                                int permissions,
                                String accessType,
                                String serviceName,
                                String resourceType,
                                String resourceName,
                                String attribute,
                                String securityClassification) {
        this(resourceId, accessorId, Permissions.fromSumOf(permissions), accessType, serviceName, resourceType,
            resourceName, attribute, securityClassification);
    }

    public int getPermissions() {
        return Permissions.sumOf(explicitPermissions);
    }

}
