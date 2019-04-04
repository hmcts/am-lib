package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class FilteredResourceEnvelope {
    private final Resource resource;
    private final AccessEnvelope access;
}
