package uk.gov.hmcts.reform.amlib.internal.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.AttributeAccessDefinition;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("PMD.ExcessiveParameterList")
public final class ExplicitAccessRecord extends AttributeAccessDefinition {
    private final String resourceId;
    private final String accessorId;
    private final AccessorType accessorType;
    private final String serviceName;
    private final String resourceType;
    private final String resourceName;
    private final JsonPointer attribute;
    private final Set<Permission> permissions;
    private final String relationship;

    private  Instant lastUpdate;

    @Setter
    private String callingServiceName;

}
