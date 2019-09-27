package uk.gov.hmcts.reform.amlib.internal.models.query;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.Setter;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.time.Instant;
import java.util.Set;
import javax.annotation.Nullable;

@Data
@Builder
@AllArgsConstructor
public final class AttributeData {
    private final JsonPointer attribute;
    private final SecurityClassification defaultSecurityClassification;
    private final Set<Permission> permissions;

    @Nullable
    private Instant lastUpdate;

    @Nullable
    @Setter
    private String callingServiceName;
}
