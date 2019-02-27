package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class DefaultRoleSetupImportServiceTest {
    private DefaultRoleSetupImportService defaultRoleSetupImportService = new DefaultRoleSetupImportService(
        "", "", "");

    @Test
    void whenServiceNameNullThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addService(null, ""));
    }

    @Test
    void whenServiceNameIsEmptyStringThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addService("", ""))
            .withMessage("Service name cannot be empty");
    }

    @Test
    void whenRoleNameIsNullThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addRole(
                null, RoleType.RESOURCE, SecurityClassification.Public, AccessManagementType.ROLEBASED));
    }

    @Test
    void whenRoleNameIsEmptyStringThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addRole(
                "", RoleType.RESOURCE, SecurityClassification.Public, AccessManagementType.ROLEBASED))
            .withMessage("Role name cannot be empty");
    }

    @Test
    void whenServiceNameIsNullThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addResourceDefinition(null, RESOURCE_TYPE, RESOURCE_NAME));
    }

    @Test
    void whenServiceNameIsEmptyThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addResourceDefinition("", RESOURCE_TYPE, RESOURCE_NAME))
            .withMessage("Service name cannot be empty");
    }

    @Test
    void whenResourceNameIsNullThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, null));
    }

    @Test
    void whenResourceNameIsEmptyThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, ""))
            .withMessage("Resource cannot contain empty values");
    }

    @Test
    void whenResourceTypeIsNullThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addResourceDefinition(SERVICE_NAME, null, RESOURCE_NAME));
    }

    @Test
    void whenResourceTypeIsEmptyThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addResourceDefinition(SERVICE_NAME, "", RESOURCE_NAME))
            .withMessage("Resource cannot contain empty values");
    }

}
