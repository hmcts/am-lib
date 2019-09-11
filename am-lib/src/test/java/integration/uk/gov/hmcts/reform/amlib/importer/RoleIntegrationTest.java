package integration.uk.gov.hmcts.reform.amlib.importer;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.internal.models.Role;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;

class RoleIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportService.class);

    private String roleName;

    @BeforeEach
    void setUp() {
        roleName = UUID.randomUUID().toString();
    }

    @Test
    void shouldAddNewEntryIntoDatabaseWhenNewRoleIsAdded() {
        service.addRole(roleName, IDAM, PUBLIC, ROLE_BASED);

        assertThat(databaseHelper.getRole(roleName)).isNotNull();
    }

    @Test
    void shouldUpdateExistingEntryWhenDuplicateRolesAreAdded() {
        service.addRole(roleName, IDAM, PUBLIC, ROLE_BASED);
        service.addRole(roleName, RoleType.RESOURCE, SecurityClassification.PRIVATE, AccessType.EXPLICIT);

        Role role = databaseHelper.getRole(roleName);
        assertThat(role).isNotNull();
        assertThat(role.getRoleType()).isEqualTo(RoleType.RESOURCE);
        assertThat(role.getSecurityClassification()).isEqualTo(SecurityClassification.PRIVATE);
        assertThat(role.getAccessType()).isEqualTo(AccessType.EXPLICIT);
    }

    @Test
    void shouldDeleteRoleFromTableWhenItExists() {
        service.addRole(roleName, IDAM, PUBLIC, ROLE_BASED);
        service.deleteRole(roleName);

        assertThat(databaseHelper.getRole(roleName)).isNull();
    }

    @Test
    void whenAuditDetailsThenShouldReturnAuditDetails() {
        //Add audit
        service.addRole(roleName, IDAM, PUBLIC, ROLE_BASED);
        Role role = databaseHelper.getRole(roleName);
        final LocalDateTime dateTime = role.getLastUpdate();
        service.addRole(roleName, RoleType.RESOURCE, SecurityClassification.PRIVATE, AccessType.EXPLICIT);
        assertThat(dateTime).isNotNull();

        //Update Audit
        role = databaseHelper.getRole(roleName);
        assertThat(role).isNotNull();
        assertThat(role.getRoleType()).isEqualTo(RoleType.RESOURCE);
        assertThat(role.getSecurityClassification()).isEqualTo(SecurityClassification.PRIVATE);
        assertThat(role.getAccessType()).isEqualTo(AccessType.EXPLICIT);
        assertThat(role.getLastUpdate()).isNotNull();
        assertThat(role.getLastUpdate()).isNotEqualTo(dateTime);
    }
}
