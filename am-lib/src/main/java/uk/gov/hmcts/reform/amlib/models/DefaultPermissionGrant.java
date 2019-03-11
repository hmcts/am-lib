package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public final class DefaultPermissionGrant {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String roleName;
    private final Map<JsonPointer, Pair<Set<Permission>, SecurityClassification>> attributePermissions;
}
