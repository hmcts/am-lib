package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.amlib.enums.AuditAction;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.time.Instant;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
public final class ResourceAttributeAudit {
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final SecurityClassification defaultSecurityClassification;

    @Setter
    private String callingServiceName;

    @NotBlank
    private final Instant auditTimeStamp;

    @Setter
    private String changedBy;

    private final AuditAction action;
}
