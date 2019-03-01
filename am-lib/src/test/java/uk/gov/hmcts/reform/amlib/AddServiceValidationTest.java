package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AddServiceValidationTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @Test
    void whenOnlyParamIsServiceNameAndItIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addService(null));
    }

    @Test
    void whenServiceNameIsNullShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addService(null, ""));
    }

    @Test
    void whenServiceNameIsEmptyShouldThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addService("", ""))
            .withMessage("Service name cannot be empty");
    }
}
