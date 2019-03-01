package integration.uk.gov.hmcts.reform.amlib.base;

import org.junit.jupiter.api.BeforeAll;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

/**
 * Base class for integration tests that populates DB with basic definitions.
 */
public class PreconfiguredIntegrationBaseTest extends IntegrationBaseTest {

    @BeforeAll
    static void populateDatabaseWithBasicDefinitions() {
        DefaultRoleSetupImportService importerService = new DefaultRoleSetupImportService(db.getJdbcUrl(),
            db.getUsername(), db.getPassword());
        importerService.addService(SERVICE_NAME);
        importerService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
    }

}
