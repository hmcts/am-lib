package integration.uk.gov.hmcts.reform.amlib.base;

import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportServiceImpl;
import uk.gov.hmcts.reform.amlib.service.DefaultRoleSetupImportService;

import java.util.UUID;

/**
 * Base class for integration tests that populates DB with basic definitions.
 */
public abstract class PreconfiguredIntegrationBaseTest extends IntegrationBaseTest {

    public String serviceName;

    @BeforeEach
    void populateDatabaseWithBasicDefinitions() {
        this.serviceName = UUID.randomUUID().toString();

        DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportServiceImpl.class);
        importerService.addService(serviceName);
    }
}
