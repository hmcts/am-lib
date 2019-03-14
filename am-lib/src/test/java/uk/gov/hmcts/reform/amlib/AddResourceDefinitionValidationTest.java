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
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(null, RESOURCE_TYPE, RESOURCE_NAME))
            .withMessageContaining("serviceName - must not be blank");
    }

    @Test
    void whenServiceNameIsEmptyShouldThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition("", RESOURCE_TYPE, RESOURCE_NAME))
            .withMessageContaining("serviceName - must not be blank");
    }

    @Test
    void whenResourceTypeIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, null, RESOURCE_NAME))
            .withMessageContaining("resourceType - must not be blank");
    }

    @Test
    void whenResourceTypeIsEmptyShouldThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, "", RESOURCE_NAME))
            .withMessageContaining("resourceType - must not be blank");
    }

    @Test
    void whenResourceNameIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, null))
            .withMessageContaining("resourceName - must not be blank");
    }

    @Test
    void whenResourceNameIsEmptyShouldThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, ""))
            .withMessageContaining("resourceName - must not be blank");
    }
}
