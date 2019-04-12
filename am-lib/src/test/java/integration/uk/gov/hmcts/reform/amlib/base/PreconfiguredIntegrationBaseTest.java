package integration.uk.gov.hmcts.reform.amlib.base;

import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

/**
 * Base class for integration tests that populates DB with basic definitions.
 */
public abstract class PreconfiguredIntegrationBaseTest extends IntegrationBaseTest {

    @BeforeEach
    void populateDatabaseWithBasicDefinitions() {
        DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
        importerService.addService(SERVICE_NAME);
        importerService.addRole(
            ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
    }
}
