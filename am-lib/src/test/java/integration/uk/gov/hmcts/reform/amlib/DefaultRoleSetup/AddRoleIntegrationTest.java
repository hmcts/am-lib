package integration.uk.gov.hmcts.reform.amlib.DefaultRoleSetup;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;


class AddRoleIntegrationTest extends IntegrationBaseTest {

    @Test
    void whenAddRoleIsCalledAddNewEntryIntoDatabase() {
        defaultRoleService.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.Public,
            AccessManagementType.ROLEBASED);
        assertThat(countRoles(ROLE_NAME)).isEqualTo(1);
    }

    @Test
    void whenCallIsMadeDuplicatingExistingRoleUpdatesExistingEntry() {
        defaultRoleService.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.Public,
            AccessManagementType.ROLEBASED);
        defaultRoleService.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.Public,
            AccessManagementType.ROLEBASED);
        assertThat(countRoles(ROLE_NAME)).isEqualTo(1);
    }
}
