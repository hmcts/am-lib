package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import org.jdbi.v3.core.mapper.Nested;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.AccessManagementAudit;

@Data
@Builder
//@AllArgsConstructor
public final class ResourceAttribute {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final SecurityClassification defaultSecurityClassification;
    @Nested
    private final AccessManagementAudit accessManagementAudit;

    public String getAttributeAsString() {
        return attribute.toString();
    }

    public ResourceAttribute(final String serviceName, final String resourceType, final String resourceName,
                             final JsonPointer attribute, final SecurityClassification defaultSecurityClassification,
                             @Nested final AccessManagementAudit accessManagementAudit) {
        this.serviceName = serviceName;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.attribute = attribute;
        this.defaultSecurityClassification = defaultSecurityClassification;
        this.accessManagementAudit = accessManagementAudit;
    }
}
