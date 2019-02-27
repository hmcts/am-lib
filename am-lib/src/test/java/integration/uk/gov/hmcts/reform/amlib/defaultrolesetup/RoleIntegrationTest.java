package integration.uk.gov.hmcts.reform.amlib.defaultrolesetup;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;

class RoleIntegrationTest extends IntegrationBaseTest {

    @Test
    void whenAddRoleIsCalledAddNewEntryIntoDatabase() {
        defaultRoleService.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);

        assertThat(countRoles(ROLE_NAME)).isEqualTo(1);
    }

    @Test
    void whenCallIsMadeDuplicatingExistingRoleUpdatesExistingEntry() {
        defaultRoleService.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        defaultRoleService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PRIVATE, AccessType.EXPLICIT);

        assertThat(countRoles(ROLE_NAME)).isEqualTo(1);
    }

    @Test
    void canDeleteRoleFromTable() {
        defaultRoleService.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        defaultRoleService.deleteRole(ROLE_NAME);

        assertThat(countRoles(ROLE_NAME)).isEqualTo(0);
    }
}
