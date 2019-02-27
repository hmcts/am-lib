package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GetRolePermissionsIntegrationTest extends IntegrationBaseTest {

    @Test
    void returnRoleBasedAccessRecord() {

        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions("Service 1", "Resource Type 1", "resource", "case");
        assertThat(accessRecord.containsKey(JsonPointer.valueOf("/test")));
    }
}
