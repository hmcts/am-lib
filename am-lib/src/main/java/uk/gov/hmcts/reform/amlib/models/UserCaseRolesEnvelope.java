package uk.gov.hmcts.reform.amlib.models;

import lombok.Builder;

import java.util.List;

@Builder
public class UserCaseRolesEnvelope {
    private String caseId;
    private String userId;
    private List<String> roles;
}
