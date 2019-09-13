package uk.gov.hmcts.reform.amlib.internal.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public final class Service {
    private final String serviceName;
    private final String serviceDescription;
    private final Instant lastUpdate;
}
