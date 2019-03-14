package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.RESOURCE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;

class AddRoleValidationTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @Test
    void whenRoleNameIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(null, RESOURCE, PUBLIC, ROLE_BASED))
            .withMessageContaining("roleName - must not be blank");
    }

    @Test
    void whenRoleNameIsEmptyShouldThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole("", RESOURCE, PUBLIC, ROLE_BASED))
            .withMessageContaining("roleName - must not be blank");
    }

    @Test
    void whenRoleTypeIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(ROLE_NAME, null, PUBLIC, ROLE_BASED))
            .withMessageContaining("roleType - must not be null");
    }

    @Test
    void whenSecurityClassificationIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(ROLE_NAME, RESOURCE, null, ROLE_BASED))
            .withMessageContaining("securityClassification - must not be null");
    }

    @Test
    void whenAccessManagementTypeIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(ROLE_NAME, RESOURCE, PUBLIC, null))
            .withMessageContaining("accessType - must not be null");
    }
}
