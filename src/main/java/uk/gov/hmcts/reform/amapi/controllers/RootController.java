package uk.gov.hmcts.reform.amapi.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.amlib.DummyService;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Default endpoints per application.
 */
@RestController
public class RootController {
    @Value("${spring.datasource.url}")
    private transient String dbUrl;

    @Value("${spring.datasource.username}")
    private transient String dbUsername;

    @Value("${spring.datasource.password}")
    private transient String dbPassword;

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
        return ok(new DummyService(dbUrl, dbUsername, dbPassword).getHello());
    }
}
