package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import uk.gov.hmcts.reform.amlib.enums.Permissions;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.amlib.enums.Permissions.hasPermissionTo;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
public class ExplicitAccessRecord extends ExplicitAccessMetadata {

    private final Set<Permissions> explicitPermissions;
    private final String securityClassification;

    @SuppressWarnings("squid:S00107") // Having so many arguments seems reasonable solution here
    @Builder(builderMethodName = "explicitAccessRecordBuilder")
    public ExplicitAccessRecord(String resourceId,
                                String accessorId,
                                Set<Permissions> explicitPermissions,
                                String accessType,
                                String serviceName,
                                String resourceType,
                                String resourceName,
                                String attribute,
                                String securityClassification) {
        super(resourceId, accessorId, accessType, serviceName, resourceType, resourceName, attribute);
        this.explicitPermissions = explicitPermissions;
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
        super(resourceId, accessorId, accessType, serviceName, resourceType, resourceName, attribute);
        this.explicitPermissions = convertSumOfPermissionsToSet(permissions);
        this.securityClassification = securityClassification;
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
