package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@SuppressWarnings("LineLength")
public final class DefaultPermissionGrant {
    @NotBlank
    private final String serviceName;
    @NotBlank
    private final String resourceType;
    @NotBlank
    private final String resourceName;
    @NotBlank
    private final String roleName;
    @NotEmpty
    private final Map<@NotNull JsonPointer, @NotNull Entry<@NotEmpty Set<@NotNull Permission>, @NotNull SecurityClassification>> attributePermissions;
}
