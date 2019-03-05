package uk.gov.hmcts.reform.amlib.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class FilterResource {
    private final String userId;
    private final Set<String> userRoles;
    private final Resource resource;
}
