package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class GetRolePermissionsIntegrationTest extends IntegrationBaseTest {

    @Test
    void returnListOfPermissionsForRoleName() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAMES);

        assertThat(accessRecord).containsEntry(JsonPointer.valueOf("/test"), READ_PERMISSION);
        assertThat(accessRecord).containsEntry(JsonPointer.valueOf("/test2"), CREATE_PERMISSION);

        assertThat(accessRecord).hasSize(2);
    }

    @Test
    void shouldReturnNullWhenServiceNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions("Service 2",
            RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAMES);
        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenResourceTypeDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            "No Resource Type ", RESOURCE_NAME, ROLE_NAMES);
        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenResourceNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            RESOURCE_TYPE, "No Resource Name", ROLE_NAMES);
        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenDefaultRoleNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            RESOURCE_TYPE, RESOURCE_NAME, Stream.of("citizen").collect(toSet()));
        assertThat(accessRecord).isNull();
    }
}
