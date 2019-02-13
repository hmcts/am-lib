package uk.gov.hmcts.reform.amapi.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.enums.Permissions;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @SuppressWarnings("unchecked") // supressing compiler warning about casting from Object to List<String>
    @PostMapping("/create-resource-access")
    public void createResourceAccess(@RequestBody Map<String, Object> amData) {

        am.createResourceAccess(ExplicitAccessRecord.explicitAccessRecordBuilder()
                .resourceId(amData.get(RESOURCE_ID_KEY).toString())
                .accessorId(amData.get("accessorId").toString())
                .explicitPermissions(((List<String>) amData.get("explicitPermissions")).stream()
                        .map(Permissions::valueOf)
                        .collect(Collectors.toSet()))
                .accessType(amData.get("accessType").toString())
                .serviceName(amData.get("serviceName").toString())
                .resourceType(amData.get("resourceType").toString())
                .resourceName(amData.get("resourceName").toString())
                .attribute(amData.get("attribute").toString())
                .securityClassification(amData.get("securityClassification").toString())
                .build());
    }

    @PostMapping("/revoke-resource-access")
    public void revokeResourceAccess(@RequestBody Map<String, Object> amData) {
        am.revokeResourceAccess(ExplicitAccessMetadata.builder()
            .resourceId(amData.get(RESOURCE_ID_KEY).toString())
            .accessorId(amData.get("accessorId").toString())
            .accessType(amData.get("accessType").toString())
            .serviceName(amData.get("serviceName").toString())
            .resourceType(amData.get("resourceType").toString())
            .resourceName(amData.get("resourceName").toString())
            .attribute(amData.get("attribute").toString())
            .build());
    }

    @PostMapping("/get-accessors-list")
    public List<String> getAccessorsList(@RequestBody Map<String, Object> amData) {
        return am.getAccessorsList(amData.get("userId").toString(), amData.get(RESOURCE_ID_KEY).toString());
    }

    @PostMapping("/filter-resource")
    public JsonNode filterResource(@RequestBody Map<String, Object> amData) {
        JsonNode jsonNode = mapper.valueToTree(amData.get("resourceJson"));
        return am.filterResource(
                amData.get("userId").toString(),
                amData.get(RESOURCE_ID_KEY).toString(),
                jsonNode
        );
    }
}
