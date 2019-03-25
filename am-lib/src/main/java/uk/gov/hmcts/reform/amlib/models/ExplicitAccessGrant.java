package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
public final class ExplicitAccessGrant {
    @NotBlank
    private final String resourceId;
    @NotEmpty
    private final Set<@NotBlank String> accessorIds;
    @NotBlank
    private final String accessType;
    @NotBlank
    private final String serviceName;
    @NotBlank
    private final String resourceType;
    @NotBlank
    private final String resourceName;
    @NotEmpty
    private final Map<@NotNull JsonPointer, @NotEmpty Set<@NotNull Permission>> attributePermissions;
    @NotNull
    private final SecurityClassification securityClassification;
}
