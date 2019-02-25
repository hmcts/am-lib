package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DefaultRoleSetupImportServiceTest {
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
    void whenRoleNameIsEmptyThrowNullPointerException() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addRole(
                null, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessManagementType.ROLEBASED));
    }

    @Test
    void whenRoleNameIsEmptyStringThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> defaultRoleSetupImportService.addRole(
                "", RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessManagementType.ROLEBASED))
            .withMessage("Role name cannot be empty");
    }

//    @ParameterizedTest()
//    @CsvSource({"\"\", resource name", "resource type, \"\""})
//    void whenResourceNameIsEmptyThrowNullPointerException(String resourceType, String resourceName) {
//        assertThatExceptionOfType(NullPointerException.class)
//            .isThrownBy(() -> defaultRoleSetupImportService.addResourceDefinition(
//                SERVICE_NAME, resourceType, resourceName)).withMessage("Resource cannot contain empty values");
//    }


}
