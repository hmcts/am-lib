package integration.uk.gov.hmcts.reform.amlib.DefaultRoleSetup;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class GrantDefaultPermissionIntegrationTest extends IntegrationBaseTest {

    @Test
    void whenAddResourceDefinitionIsCalledAddNewEntryIntoDatabase() {
        defaultRoleService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.Public, AccessManagementType.ROLEBASED);

        Pair<Set<Permission>, SecurityClassification> pair = new Pair<>(READ_PERMISSION, SecurityClassification.Public);
        Map<JsonPointer, Pair<Set<Permission>, SecurityClassification>> attributePermission = new ConcurrentHashMap<>();
        attributePermission.put(JsonPointer.valueOf("/test"), pair);

        defaultRoleService.grantDefaultPermission(ROLE_NAME, SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, attributePermission);

        assertThat(countDefaultPermissions()).isEqualTo(1);
        assertThat(countResourceAttributes()).isEqualTo(1);
    }
}
