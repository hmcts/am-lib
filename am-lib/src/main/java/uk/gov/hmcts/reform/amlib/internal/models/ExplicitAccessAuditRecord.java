package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.AuditAction;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.time.Instant;
import java.util.Set;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
public class ExplicitAccessAuditRecord {

    private final String resourceId;
    private final String accessorId;
    private final AccessorType accessorType;
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final Set<Permission> permissions;
    private final String relationship;

    @Setter
    private String callingServiceName;

    @NotBlank
    private final Instant auditTimeStamp;

    @Setter
    private String changedBy;

    private AuditAction action;
}
