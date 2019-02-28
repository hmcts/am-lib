package integration.uk.gov.hmcts.reform.amlib.defaultrolesetup;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createReadPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class DefaultPermissionIntegrationTest extends IntegrationBaseTest {

    @Test
    void shouldNotBeAbleToCreateDefaultPermissionWhenRoleDoesNotExist() {
        defaultRoleService.deleteRole(ROLE_NAME);

        assertThatExceptionOfType(PersistenceException.class)
            .isThrownBy(() -> defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION)))
            .withMessageContaining("(role_name)=(Role Name) is not present in table \"roles\"");
    }

    @Test
    void whenAddResourceDefinitionIsCalledAddNewEntryIntoDatabase() {
        defaultRoleService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));

        assertThat(countDefaultPermissions(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, ROLE_NAME, Permissions.sumOf(READ_PERMISSION)))
            .isEqualTo(1);

        assertThat(countResourceAttributes(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, SecurityClassification.PUBLIC))
            .isEqualTo(1);
    }

    @Test
    void whenAddResourceDefinitionIsCalledTwiceWithSameParamsOverwriteExistingRecord() {
        defaultRoleService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));
        defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant(CREATE_PERMISSION));

        assertThat(countDefaultPermissions(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, ROLE_NAME, Permissions.sumOf(CREATE_PERMISSION)))
            .isEqualTo(1);

        assertThat(countResourceAttributes(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, SecurityClassification.PUBLIC))
            .isEqualTo(1);
    }

    @Test
    void truncateDefaultPermissionsRemovesAllEntriesFromTables() {
        defaultRoleService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME + "2");

        defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));
        defaultRoleService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME + "2")
            .attributePermissions(createReadPermissionsForAttribute(READ_PERMISSION))
            .build());

        defaultRoleService.truncateDefaultPermissionsForService(SERVICE_NAME, RESOURCE_TYPE);

        assertThat(countDefaultPermissions(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, ROLE_NAME, Permissions.sumOf(READ_PERMISSION)))
            .isEqualTo(0);

        assertThat(countResourceAttributes(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, SecurityClassification.PUBLIC))
            .isEqualTo(0);
    }

    @Test
    void truncateDefaultPermissionsRemovesEntriesWithResourceNameFromTables() {
        defaultRoleService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        defaultRoleService.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));

        defaultRoleService.truncateDefaultPermissionsByResourceDefinition(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);

        assertThat(countDefaultPermissions(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, ROLE_NAME, Permissions.sumOf(READ_PERMISSION)))
            .isEqualTo(0);

        assertThat(countResourceAttributes(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, SecurityClassification.PUBLIC))
            .isEqualTo(0);
    }
}
