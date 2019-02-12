package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import uk.gov.hmcts.reform.amlib.enums.Permissions;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.amlib.enums.Permissions.hasPermissionTo;

@Data
@Builder
public class ExplicitAccessRecord {

    private final String resourceId;
    private final String accessorId;
    private final Set<Permissions> explicitPermissions;
    private final String accessType;
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String attribute;
    private final String securityClassification;

    @SuppressWarnings("squid:S00107") // Having so many arguments seems reasonable solution here
    public ExplicitAccessRecord(String resourceId,
                                String accessorId,
                                Set<Permissions> explicitPermissions,
                                String accessType,
                                String serviceName,
                                String resourceType,
                                String resourceName,
                                String attribute,
                                String securityClassification) {
        this.resourceId = resourceId;
        this.accessorId = accessorId;
        this.explicitPermissions = explicitPermissions;
        this.accessType = accessType;
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.attribute = attribute;
        this.securityClassification = securityClassification;
    }

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
        this(resourceId, accessorId, convertSumOfPermissionsToSet(permissions), accessType, serviceName, resourceType,
                resourceName, attribute, securityClassification);
    }

    public int getPermissions() {
        return Permissions.sumOf(explicitPermissions);
    }

    private static Set<Permissions> convertSumOfPermissionsToSet(int sumOfPermissions) {
        return Arrays.stream(Permissions.values())
                .filter(permission -> hasPermissionTo(sumOfPermissions, permission))
                .collect(Collectors.toSet());
    }

}
