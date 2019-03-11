package uk.gov.hmcts.reform.amlib.internal.repositories.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public final class Service {
    private final String serviceName;
    private final String serviceDescription;
}
