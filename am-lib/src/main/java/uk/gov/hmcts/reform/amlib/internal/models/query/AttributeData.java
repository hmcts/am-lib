package uk.gov.hmcts.reform.amlib.internal.models.query;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;

import org.jdbi.v3.core.mapper.Nested;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.AccessManagementAudit;

import java.util.Set;
import javax.annotation.Nullable;

@Data
@Builder
//@AllArgsConstructor
public final class AttributeData {
    private final JsonPointer attribute;
    private final SecurityClassification defaultSecurityClassification;
    private final Set<Permission> permissions;

    @Nested
    private final AccessManagementAudit accessManagementAudit;

    public AttributeData(final JsonPointer attribute, final SecurityClassification defaultSecurityClassification,
                         final Set<Permission> permissions,
                         @Nullable @Nested final AccessManagementAudit accessManagementAudit) {
        this.attribute = attribute;
        this.defaultSecurityClassification = defaultSecurityClassification;
        this.permissions = permissions;
        this.accessManagementAudit = accessManagementAudit;
    }
}
