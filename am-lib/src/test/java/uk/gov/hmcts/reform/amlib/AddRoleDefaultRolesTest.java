package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;

class AddRoleDefaultRolesTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @Test
    void whenRoleNameIsNullAddRoleWillThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addRole(
                null, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED));
    }

    @Test
    void whenRoleNameIsEmptyAddRoleWillThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(
                "", RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED))
            .withMessage("Role name cannot be empty");
    }

    @Test
    void whenRoleTypeIsNullAddRoleWillThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addRole(
                ROLE_NAME, null, SecurityClassification.PUBLIC, AccessType.ROLE_BASED));
    }

    @Test
    void whenSecurityClassificationIsNullAddRoleWillThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addRole(
                ROLE_NAME, RoleType.RESOURCE, null, AccessType.ROLE_BASED));
    }

    @Test
    void whenAccessManagementTypeIsNullAddRoleWillThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addRole(
                ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, null));
    }
}
