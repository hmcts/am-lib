package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permissions;

@Data
@Builder
public class CreateResource {

    private final String resourceId;
    private final String accessorId;
    private final ExplicitPermissions explicitPermissions;
    private final String accessType;
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String attribute;
    private final String securityClassification;

    public CreateResource(final String resourceId,
                          final String accessorId,
                          final ExplicitPermissions explicitPermissions,
                          final String accessType,
                          final String serviceName,
                          final String resourceType,
                          final String resourceName,
                          final String attribute,
                          final String securityClassification) {
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

    public int getPermissions() {
        return Permissions.sumOf(explicitPermissions.getUserPermissions());
    }
}
