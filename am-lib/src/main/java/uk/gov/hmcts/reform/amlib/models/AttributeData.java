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
public final class AttributeData {
    private final JsonPointer attribute;
    private final SecurityClassification defaultSecurityClassification;
    private final Set<Permission> permissions;
}
