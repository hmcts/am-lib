package uk.gov.hmcts.reform.amapi.controllers;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.model.AccessManagement;

import java.util.HashMap;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Default endpoints per application.
 */
@RestController
@SuppressWarnings("PMD")
public class RootController {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @PostMapping("/")
    public ResponseEntity<Integer> welcome(@RequestBody HashMap<String, Object> am) {
        AccessManagementService dm = new AccessManagementService(Jdbi.create(dbUrl, dbUsername, dbPassword));
        return ok(dm.createResourceAccess(am.get("resourceId").toString(), am.get("accessorId").toString()));
    }
}
