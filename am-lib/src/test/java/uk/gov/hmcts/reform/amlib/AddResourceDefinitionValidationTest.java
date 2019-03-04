package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class AddResourceDefinitionValidationTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @Test
    void whenServiceNameIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addResourceDefinition(null, RESOURCE_TYPE, RESOURCE_NAME));
    }

    @Test
    void whenServiceNameIsEmptyShouldThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition("", RESOURCE_TYPE, RESOURCE_NAME))
            .withMessage("Service name cannot be empty");
    }

    @Test
    void whenResourceTypeIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, null, RESOURCE_NAME));
    }

    @Test
    void whenResourceTypeIsEmptyShouldThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, "", RESOURCE_NAME))
            .withMessage("Resource cannot contain empty values");
    }

    @Test
    void whenResourceNameIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, null));
    }

    @Test
    void whenResourceNameIsEmptyShouldThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, ""))
            .withMessage("Resource cannot contain empty values");
    }
}
