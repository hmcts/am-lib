package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class UserCaseRolesEnvelope {
    private String caseId;
    private String userId;
    private List<String> roles;
}
