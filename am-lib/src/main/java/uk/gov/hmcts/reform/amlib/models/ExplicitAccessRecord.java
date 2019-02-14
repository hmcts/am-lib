package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import uk.gov.hmcts.reform.amlib.enums.Permissions;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.amlib.enums.Permissions.hasPermissionTo;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExplicitAccessRecord extends AbstractAccessMetadata {

    private final Set<Permissions> explicitPermissions;

    @Builder // All args constructor is needs for builder. @SuperBuilder cannot be used because IDE does not support it
    @SuppressWarnings("squid:S00107") // Having so many arguments seems reasonable solution here
    private ExplicitAccessRecord(String resourceId,
                                 String accessorId,
                                 String accessType,
                                 String serviceName,
                                 String resourceType,
                                 String resourceName,
                                 String attribute,
                                 String securityClassification,
                                 Set<Permissions> explicitPermissions) {
        super(resourceId, accessorId, accessType, serviceName, resourceType, resourceName, attribute,
                securityClassification);
        this.explicitPermissions = explicitPermissions;
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
        super(resourceId, accessorId, accessType, serviceName, resourceType, resourceName, attribute, securityClassification);
        this.explicitPermissions = convertSumOfPermissionsToSet(permissions);
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
