package uk.gov.hmcts.reform.amapi.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;

import javax.transaction.TransactionRolledbackException;

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
    private ObjectMapper mapper;
    @Autowired
    private AccessManagementService am;

    @PostMapping("/create-resource-access")
    public void createResourceAccess(@RequestBody ExplicitAccessGrant amData) {
        am.grantExplicitResourceAccess(amData);
    }

    @PostMapping("/revoke-resource-access")
    public void revokeResourceAccess(@RequestBody ExplicitAccessMetadata amData) {
        am.revokeResourceAccess(amData);
    }

    @PostMapping("/get-accessors-list")
    public List<String> getAccessorsList(@RequestBody Map<String, Object> amData) {
        return am.getAccessorsList(amData.get("userId").toString(), amData.get(RESOURCE_ID_KEY).toString());
    }

    @PostMapping("/filter-resource")
    public FilterResourceResponse filterResource(@RequestBody Map<String, Object> amData) {
        JsonNode jsonNode = mapper.valueToTree(amData.get("resourceJson"));
        return am.filterResource(
            amData.get("userId").toString(),
            amData.get(RESOURCE_ID_KEY).toString(),
            jsonNode
        );
    }
}
