package uk.gov.hmcts.reform.amapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/")
    public void createResourceAccess(@RequestBody HashMap<String, Object> amData) throws AccessManagementException {
        am.createResourceAccess(amData.get("resourceId").toString(), amData.get("accessorId").toString());
    }
}
