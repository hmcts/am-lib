package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME_IN_LIST;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class GetRolePermissionsIntegrationTest extends IntegrationBaseTest {

    @Test
    void returnListOfPermissionsForRoleName() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAME_IN_LIST);

        assertThat(accessRecord).containsEntry(JsonPointer.valueOf("/test"),READ_PERMISSION);
        assertThat(accessRecord).containsEntry(JsonPointer.valueOf("/test2"),CREATE_PERMISSION);

        assertThat(accessRecord.size() == 2);
    }

    @Test
    void nonExistentServiceReturnsNull() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions("Service 2",
            RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAME_IN_LIST);
        assertThat(accessRecord).isNull();
    }

    @Test
    void noResourceTypeReturnsNull() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            "No Resource Type ", RESOURCE_NAME, ROLE_NAME_IN_LIST);
        assertThat(accessRecord).isNull();
    }

    @Test
    void noDefaultRoleNameReturnsNull() {
        List<String> noDefaultRoleName = Arrays.asList("citizen");

        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            RESOURCE_TYPE, RESOURCE_NAME, noDefaultRoleName);
        assertThat(accessRecord).isNull();
    }
}
