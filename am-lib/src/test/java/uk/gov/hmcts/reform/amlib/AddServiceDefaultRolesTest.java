package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AddServiceDefaultRolesTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @Test
    void whenServiceNameIsNullAddServiceShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addService(null, ""));
    }

    @Test
    void whenOnlyParamIsServiceNameAndItIsNullAddServiceShouldThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> service.addService(null));
    }

    @Test
    void whenServiceNameIsEmptyAddServiceShouldThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addService("", ""))
            .withMessage("Service name cannot be empty");
    }
}
