package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public final class RolePermissions {
    private final Map<JsonPointer, Set<Permission>> permissions;
    private final Map<JsonPointer, SecurityClassification> securityClassifications;
    private final SecurityClassification roleSecurityClassification;
    private final AccessType roleAccessType;
}
