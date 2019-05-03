package integration.uk.gov.hmcts.reform.amlib.base;

import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import java.util.UUID;

import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.RESOURCE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PRIVATE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.PRIVATE_ROLE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;

/**
 * Base class for integration tests that populates DB with basic definitions.
 */
public abstract class PreconfiguredIntegrationBaseTest extends IntegrationBaseTest {

    public String serviceName;

    @BeforeEach
    void populateDatabaseWithBasicDefinitions() {
        this.serviceName = UUID.randomUUID().toString();

        DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
        importerService.addService(serviceName);
        importerService.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        importerService.addRole(OTHER_ROLE_NAME, IDAM, PUBLIC, EXPLICIT);
        importerService.addRole(PRIVATE_ROLE, RESOURCE, PRIVATE, ROLE_BASED);
        importerService.addResourceDefinition(
            createResourceDefinition(serviceName, UUID.randomUUID().toString(), UUID.randomUUID().toString()));
    }
}
