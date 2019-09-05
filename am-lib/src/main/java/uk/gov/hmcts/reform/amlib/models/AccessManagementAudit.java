package uk.gov.hmcts.reform.amlib.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = AccessManagementAudit.AccessManagementAuditBuilder.class)
public class AccessManagementAudit {

    @NotBlank
    private final LocalDateTime lastUpdate;

    @NotBlank
    private final String callerService;
}
