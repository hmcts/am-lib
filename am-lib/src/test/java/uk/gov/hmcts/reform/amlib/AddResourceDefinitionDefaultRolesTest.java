package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class AddResourceDefinitionDefaultRolesTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @Test
    void whenServiceNameIsNullThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addResourceDefinition(null, RESOURCE_TYPE, RESOURCE_NAME));
    }

    @Test
    void whenServiceNameIsEmptyThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition("", RESOURCE_TYPE, RESOURCE_NAME))
            .withMessage("Service name cannot be empty");
    }

    @Test
    void whenResourceNameIsNullThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, null));
    }

    @Test
    void whenResourceNameIsEmptyThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, ""))
            .withMessage("Resource cannot contain empty values");
    }

    @Test
    void whenResourceTypeIsNullThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, null, RESOURCE_NAME));
    }

    @Test
    void whenResourceTypeIsEmptyThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, "", RESOURCE_NAME))
            .withMessage("Resource cannot contain empty values");
    }
}
