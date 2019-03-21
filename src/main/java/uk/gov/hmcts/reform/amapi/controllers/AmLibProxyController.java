package uk.gov.hmcts.reform.amapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amapi.models.FilterResource;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;

import java.util.List;
import java.util.Map;

/**
 * Default endpoints per application.
 */
@RestController
@RequestMapping("lib")
public class AmLibProxyController {

    private static final String RESOURCE_ID_KEY = "resourceId";

    @Autowired
    private AccessManagementService am;

    @PostMapping("/create-resource-access")
    public void createResourceAccess(@RequestBody ExplicitAccessGrant amData) {
        am.grantExplicitResourceAccess(amData);
    }

    @PostMapping("/revoke-resource-access")
    public void revokeResourceAccess(@RequestBody ExplicitAccessMetadata request) {
        am.revokeResourceAccess(request);
    }

    @PostMapping("/get-accessors-list")
    public List<String> getAccessorsList(@RequestBody Map<String, Object> request) {
        return am.getAccessorsList(request.get("userId").toString(), request.get(RESOURCE_ID_KEY).toString());
    }

    @PostMapping("/filter-resource")
    public FilteredResourceEnvelope filterResource(@RequestBody FilterResource request) {
        return am.filterResource(request.getUserId(), request.getUserRoles(), request.getResource());
    }
}
