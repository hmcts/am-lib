package uk.gov.hmcts.reform.amlib.internal.models.query;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public final class AttributeData {
    private final JsonPointer attribute;
    private final SecurityClassification defaultSecurityClassification;
    private final Set<Permission> permissions;
}
