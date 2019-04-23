package integration.uk.gov.hmcts.reform.amlib.base;

import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.RESOURCE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
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
        importerService.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        importerService.addRole(OTHER_ROLE_NAME, IDAM, PUBLIC, EXPLICIT);
        importerService.addResourceDefinition(ResourceDefinition.builder()
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .build());
    }
}
