package integration.uk.gov.hmcts.reform.amlib.defaultrolesetup;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;

class RoleIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service;

    @BeforeAll
    static void setUp() {
        service = new DefaultRoleSetupImportService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
    }

    @Test
    void shouldAddNewEntryIntoDatabaseWhenNewRoleIsAdded() {
        service.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);

        assertThat(countRoles(ROLE_NAME).size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateExistingEntryWhenDuplicateRolesAreAdded() {
        service.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        service.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PRIVATE, AccessType.EXPLICIT);

        assertThat(countRoles(ROLE_NAME).size()).isEqualTo(1);
        assertThat(countRoles(ROLE_NAME).get(0).values()).containsExactly(
            ROLE_NAME,
            RoleType.RESOURCE.toString(),
            SecurityClassification.PRIVATE.toString(),
            AccessType.EXPLICIT.toString());
    }

    @Test
    void shouldDeleteRoleFromTableWhenItExists() {
        service.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        service.deleteRole(ROLE_NAME);

        assertThat(countRoles(ROLE_NAME)).isEmpty();
    }
}
