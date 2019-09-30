package uk.gov.hmcts.reform.amapi.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.amapi.models.FilterResource;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.FilterResourceService;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.ResourceAccessorsEnvelope;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Default endpoints per application.
 */
@RestController
@RequestMapping("api/${version:v1}")
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.ConfusingTernary"})
public class AccessManagementController {

    @Autowired
    private AccessManagementService accessManagementService;

    @Autowired
    FilterResourceService filterResourceService;


    @ApiOperation(value = "Grant resource access to user", response = ExplicitAccessGrant.class)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully granted the access to resource for user"),
        @ApiResponse(code = 415, message = "Unsupported media type.Excepted json"),
        @ApiResponse(code = 401, message = "You are not authorized to perform this particular request. "
            + "Please provide a valid access token in the request"),
        @ApiResponse(code = 400, message = "Incomplete request information or Malformed input request"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @PostMapping(value = "/access-resource", consumes = (APPLICATION_JSON_VALUE))
    @ResponseStatus(CREATED)
    public ResponseEntity<ExplicitAccessGrant> createResourceAccess(@RequestBody ExplicitAccessGrant
                                                                        explicitAccessGrantData,
                                                                    @RequestHeader(value = "callingServiceName",
                                                                        required = false)
                                                                        String callingServiceName,
                                                                    @RequestHeader(value = "changedBy",
                                                                        required = false) String changedBy) {

        explicitAccessGrantData.setCallingServiceName(callingServiceName);
        explicitAccessGrantData.setChangedBy(changedBy);
        accessManagementService.grantExplicitResourceAccess(explicitAccessGrantData);
        return new ResponseEntity<>(explicitAccessGrantData, CREATED);
    }

    @ApiOperation("Revoke resource access to user")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Successfully revoked user access for resource"),
        @ApiResponse(code = 415, message = "Unsupported media type.Excepted json"),
        @ApiResponse(code = 401, message = "You are not authorized to perform this particular request. "
            + "Please provide a valid access token in the request"),
        @ApiResponse(code = 400, message = "Incomplete request information or Malformed input request"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @DeleteMapping(value = "/access-resource", consumes = (APPLICATION_JSON_VALUE))
    @ResponseStatus(NO_CONTENT)
    public ResponseEntity<Void> revokeResourceAccess(@RequestBody ExplicitAccessMetadata request,
                                                     @RequestHeader(value = "callingServiceName", required = false)
                                                         String callingServiceName,
                                                     @RequestHeader(value = "changedBy", required = false)
                                                         String changedBy) {

        request.setCallingServiceName(callingServiceName);
        request.setChangedBy(changedBy);
        accessManagementService.revokeResourceAccess(request);
        return new ResponseEntity<>(NO_CONTENT);
    }

    @ApiOperation(value = "Filter access to resource", response = ExplicitAccessGrant.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully returns filtered output"),
        @ApiResponse(code = 415, message = "Unsupported media type. Expected json"),
        @ApiResponse(code = 401, message = "You are not authorized to perform this particular request. "
            + "Please provide a valid access token in the request"),
        @ApiResponse(code = 400, message = "Incomplete request information or Malformed input request"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @PostMapping(value = "/filter-resource", consumes = (APPLICATION_JSON_VALUE))
    public FilteredResourceEnvelope filterResource(@RequestBody FilterResource request) {
        return filterResourceService.filterResource(request.getUserId(), request.getUserRoles(),
            request.getResource(), request.getAttributeSecurityClassification());
    }

    @ApiOperation(value = "Finds users who have explicit access to a resource",
        response = ResourceAccessorsEnvelope.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully returns users who have access to a resource"),
        @ApiResponse(code = 401, message = "You are not authorized to perform this particular request. "
            + "Please provide a valid access token in the request"),
        @ApiResponse(code = 400, message = "Incomplete request information or malformed input request"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @GetMapping("/resource/resourceType/{resourceType}/resourceName/{resourceName}/resourceId/{resourceId}")
    public ResourceAccessorsEnvelope returnResourceAccessors(@PathVariable("resourceType") String resourceType,
                                                             @PathVariable("resourceName") String resourceName,
                                                             @PathVariable("resourceId") String resourceId) {
        return filterResourceService.returnResourceAccessors(resourceId, resourceName, resourceType);
    }
}
