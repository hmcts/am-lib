package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AddServiceValidationTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @Test
    void whenOnlyParamIsServiceNameAndItIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addService(null))
            .withMessageContaining("serviceName - must not be blank");
    }

    @Test
    void whenServiceNameIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addService(null, ""))
            .withMessageContaining("serviceName - must not be blank");
    }

    @Test
    void whenServiceNameIsEmptyShouldThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addService("", ""))
            .withMessageContaining("serviceName - must not be blank");
    }
}
