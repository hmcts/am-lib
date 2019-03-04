package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;

class AddRoleValidationTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @Test
    void whenRoleNameIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addRole(
                null, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED));
    }

    @Test
    void whenRoleNameIsEmptyShouldThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(
                "", RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED))
            .withMessage("Role name cannot be empty");
    }

    @Test
    void whenRoleTypeIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addRole(
                ROLE_NAME, null, SecurityClassification.PUBLIC, AccessType.ROLE_BASED));
    }

    @Test
    void whenSecurityClassificationIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addRole(
                ROLE_NAME, RoleType.RESOURCE, null, AccessType.ROLE_BASED));
    }

    @Test
    void whenAccessManagementTypeIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addRole(
                ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, null));
    }
}
