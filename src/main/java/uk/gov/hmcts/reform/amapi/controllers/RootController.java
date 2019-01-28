package uk.gov.hmcts.reform.amapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.exceptions.AccessManagementException;

import java.util.HashMap;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Default endpoints per application.
 */
@RestController
@SuppressWarnings("PMD")
public class RootController {

    @Autowired AccessManagementService am;

    /**
     * Root GET endpoint.
     *
     * <p>Azure application service has a hidden feature of making requests to root endpoint when
         * "Always On" is turned on.
     * This is the endpoint to deal with that and therefore silence the unnecessary 404s as a response code.
     *
             * @return Welcome message from the service.
     */
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ok("Welcome to AM Lib Testing Service!");
    }

    @PostMapping("/create-resource-access")
    public void createResourceAccess(@RequestBody HashMap<String, Object> amData) throws AccessManagementException {
        am.createResourceAccess(amData.get("resourceId").toString(), amData.get("accessorId").toString());
    }
}
