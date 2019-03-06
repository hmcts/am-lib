package uk.gov.hmcts.reform.amapi.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.amlib.models.Resource;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class FilterResource {
    private final String userId;
    private final Set<String> userRoles;
    private final Resource resource;
}
