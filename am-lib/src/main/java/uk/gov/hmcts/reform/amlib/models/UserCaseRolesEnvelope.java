package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Builder
@Data
public class UserCaseRolesEnvelope {
    private String caseId;
    private String userId;
    private Set<String> roles;
}
