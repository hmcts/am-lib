package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.utils.Pair;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class DefaultPermissionGrant {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final String roleName;
    private final Map<JsonPointer, Pair<Set<Permission>, SecurityClassification>> attributePermissions;
}
