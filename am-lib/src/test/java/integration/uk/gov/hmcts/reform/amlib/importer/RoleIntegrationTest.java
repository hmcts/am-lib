package integration.uk.gov.hmcts.reform.amlib.importer;

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

        assertThat(countRoles(ROLE_NAME)).hasSize(1);
    }

    @Test
    void shouldUpdateExistingEntryWhenDuplicateRolesAreAdded() {
        service.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        service.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PRIVATE, AccessType.EXPLICIT);

        assertThat(countRoles(ROLE_NAME)).hasSize(1);
        assertThat(countRoles(ROLE_NAME).get(0)).containsValues(
            ROLE_NAME,
            RoleType.RESOURCE.name(),
            SecurityClassification.PRIVATE.name(),
            AccessType.EXPLICIT.name());
    }

    @Test
    void shouldDeleteRoleFromTableWhenItExists() {
        service.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        service.deleteRole(ROLE_NAME);

        assertThat(countRoles(ROLE_NAME)).isEmpty();
    }
}
