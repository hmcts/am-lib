package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public final class FilteredResourceEnvelope {
    private final Resource resource;
    private Set<String> relationships;
    private final AccessEnvelope access;
}
