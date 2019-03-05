package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class GetRolePermissionsValidationTest {

    private final AccessManagementService service = new AccessManagementService("", "", "");

    @Test
    void shouldThrowNullPointerWhenServiceNameIsNull() {
        assertThrows(NullPointerException.class, () -> {
            service.getRolePermissions(null, RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAMES);
        });
    }

    @Test
    void shouldThrowNullPointerWhenResourceTypeIsNull() {
        assertThrows(NullPointerException.class, () -> {
            service.getRolePermissions(SERVICE_NAME, null, RESOURCE_NAME, ROLE_NAMES);
        });
    }

    @Test
    void shouldThrowNullPointerWhenResourceNameIsNull() {
        assertThrows(NullPointerException.class, () -> {
            service.getRolePermissions(SERVICE_NAME, RESOURCE_TYPE, null, ROLE_NAMES);
        });
    }

    @Test
    void shouldThrowNullPointerWhenRoleIsNull() {
        assertThrows(NullPointerException.class, () -> {
            service.getRolePermissions(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, null);
        });
    }
}
