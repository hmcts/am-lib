package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public class ExplicitAccessGrant {

    private final String resourceId;
    private final String accessorId;
    private final String accessType;
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final Map<JsonPointer, Set<Permission>> attributePermissions;
    private final String securityClassification;

    public ExplicitAccessGrant(String resourceId,
                               String accessorId,
                               String accessType,
                               String serviceName,
                               String resourceType,
                               String resourceName,
                               Map<JsonPointer, Set<Permission>> attributePermissions,
                               String securityClassification) {
        this.resourceId = resourceId;
        this.accessorId = accessorId;
        this.accessType = accessType;
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.attributePermissions = attributePermissions;
        this.securityClassification = securityClassification;
    }
}
