package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DefaultRoleSetupImportServiceTest {
    DefaultRoleSetupImportService defaultRoleSetupImportService =
        new DefaultRoleSetupImportService("", "", "");

    @Test
    void whenServiceNameNullThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addService(null, ""));
    }

    @Test
    void whenServiceNameEmptyString() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addService("", ""))
            .withMessage("Service name cannot be empty");
    }
}
