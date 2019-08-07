package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.UUID;

import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;

public class ReturnCaseRolesForUserIntegrationTest extends PreconfiguredIntegrationBaseTest {

    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);

    private String resourceId;
    private String accessorId;
    private String idamRoleWithExplicitAccess;
    private ResourceDefinition resourceDefinition;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();

        importerService.addRole(idamRoleWithExplicitAccess = UUID.randomUUID().toString(), IDAM, PUBLIC, EXPLICIT);
        importerService.addResourceDefinition(resourceDefinition =
            createResourceDefinition(serviceName, "case", UUID.randomUUID().toString()));
    }

    @Test
    void whenUserHasNoAccessToCaseShouldReturnEmptyListOfRoles() {

    }

    @Test
    void whenUserHasAttributeLevelAccessToCaseShouldReturnEmptyListOfRoles() {

    }
}
