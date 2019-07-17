package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public class ResourceAccessor {
    private final String accessorId;
    private final AccessorType accessorType;
    private final Set<String> relationships;
    private final Map<JsonPointer, Set<Permission>> permissions;
}
