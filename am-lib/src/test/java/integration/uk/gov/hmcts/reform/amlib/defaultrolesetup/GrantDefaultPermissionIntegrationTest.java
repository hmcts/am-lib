package integration.uk.gov.hmcts.reform.amlib.defaultrolesetup;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createReadPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class GrantDefaultPermissionIntegrationTest extends IntegrationBaseTest {

    @Test
    void whenAddResourceDefinitionIsCalledAddNewEntryIntoDatabase() {
        defaultRoleService.addRole(
            ROLE_NAME, RoleType.RESOURCE, SecurityClassification.Public, AccessManagementType.ROLEBASED);

        defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant());

        assertThat(countDefaultPermissions()).isEqualTo(1);
        assertThat(countResourceAttributes()).isEqualTo(1);
    }

    @Test
    void whenAddResourceDefinitionIsCalledTwiceWithSameParamsOverwriteExistingRecord() {
        defaultRoleService.addRole(
            ROLE_NAME, RoleType.RESOURCE, SecurityClassification.Public, AccessManagementType.ROLEBASED);

        defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant());
        defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant());

        assertThat(countDefaultPermissions()).isEqualTo(1);
        assertThat(countResourceAttributes()).isEqualTo(1);
    }

    @Test
    void truncateDefaultPermissionsRemovesAllEntriesFromTables() {
        defaultRoleService.addRole(
            ROLE_NAME, RoleType.RESOURCE, SecurityClassification.Public, AccessManagementType.ROLEBASED);
        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME + "2");

        defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant());
        defaultRoleService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME + "2")
            .attributePermissions(createReadPermissionsForAttribute())
            .build());

        defaultRoleService.truncateAllDefaultPermissionsForService(SERVICE_NAME, RESOURCE_TYPE);

        assertThat(countDefaultPermissions()).isEqualTo(0);
        assertThat(countResourceAttributes()).isEqualTo(0);
    }

    @Test
    void truncateDefaultPermissionsRemovesEntriesWithResourceNameFromTables() {
        defaultRoleService.addRole(
            ROLE_NAME, RoleType.RESOURCE, SecurityClassification.Public, AccessManagementType.ROLEBASED);
        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant());


        defaultRoleService.truncateAllDefaultPermissionsByResourceDefinition(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);

        assertThat(countDefaultPermissions()).isEqualTo(0);
        assertThat(countResourceAttributes()).isEqualTo(0);
    }
}
