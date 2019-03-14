package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class GetRolePermissionsValidationTest {

    private final AccessManagementService service = new AccessManagementService("", "", "");

    @Test
    void shouldThrowExceptionWhenServiceNameIsNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(null, RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAMES))
            .withMessageContaining("serviceName - must not be blank");
    }

    @Test
    void shouldThrowExceptionWhenResourceTypeIsNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(SERVICE_NAME, null, RESOURCE_NAME, ROLE_NAMES))
            .withMessageContaining("resourceType - must not be blank");
    }

    @Test
    void shouldThrowExceptionWhenResourceNameIsNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(SERVICE_NAME, RESOURCE_TYPE, null, ROLE_NAMES))
            .withMessageContaining("resourceName - must not be blank");
    }

    @Test
    void shouldThrowExceptionWhenRolesListIsNull() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, null))
            .withMessageContaining("roleNames - must not be null");
    }

    @Test
    void shouldThrowExceptionWhenRolesListHasEmptyElement() {
        Set<String> emptyRoleNames = ImmutableSet.of("");
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, emptyRoleNames))
            .withMessageContaining("roleNames[].<iterable element> - must not be blank");
    }
}
