package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

public class RolePermissionTest {

    private final AccessManagementService ams = new AccessManagementService("","","");

    @Test
    void shouldThrowNullPointerWhenServiceNameIsNull() {
        assertThrows(NullPointerException.class, () ->
        {ams.getRolePermissions(null, RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAME);});
    }

    @Test
    void shouldThrowNullPointerWhenResourceTypeIsNull() {
        assertThrows(NullPointerException.class, () ->
        {ams.getRolePermissions(SERVICE_NAME,null, RESOURCE_NAME, ROLE_NAME);});
    }

    @Test
    void shouldThrowNullPointerWhenResourceNameIsNull() {
        assertThrows(NullPointerException.class, () ->
        {ams.getRolePermissions(SERVICE_NAME, RESOURCE_TYPE,null , ROLE_NAME);});
    }

    @Test
    void shouldThrowNullPointerWhenRoleIsNull() {
        assertThrows(NullPointerException.class, () ->
        {ams.getRolePermissions(SERVICE_NAME,RESOURCE_TYPE,RESOURCE_NAME,null);});
    }
}
