package integration.uk.gov.hmcts.reform.amlib.importer;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
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
    private static DefaultRoleSetupImportService service;

    @BeforeAll
    static void setUp() {
        service = new DefaultRoleSetupImportService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
        service.addService(SERVICE_NAME);
        service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
    }

    @Test
    void shouldNotBeAbleToCreateDefaultPermissionWhenRoleDoesNotExist() {
        assertThatExceptionOfType(PersistenceException.class)
            .isThrownBy(() -> service.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION)))
            .withMessageContaining("(role_name)=(Role Name) is not present in table \"roles\"");
    }

    @Test
    void shouldAddNewEntryIntoDatabaseWhenUniqueEntry() {
        service.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        service.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));

        assertThat(countDefaultPermissions(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, ROLE_NAME, Permissions.sumOf(READ_PERMISSION)))
            .isEqualTo(1);

        assertThat(countResourceAttributes(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, SecurityClassification.PUBLIC))
            .isEqualTo(1);
    }

    @Test
    void shouldOverwriteExistingRecordWhenEntryIsAddedASecondTime() {
        service.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        service.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));
        service.grantDefaultPermission(createDefaultPermissionGrant(CREATE_PERMISSION));

        assertThat(countDefaultPermissions(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, ROLE_NAME, Permissions.sumOf(CREATE_PERMISSION)))
            .isEqualTo(1);

        assertThat(countResourceAttributes(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, SecurityClassification.PUBLIC))
            .isEqualTo(1);
    }

    @Test
    void shouldRemoveAllEntriesFromTablesWhenValuesExist() {
        service.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME + "2");

        service.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));
        service.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME + "2")
            .attributePermissions(createReadPermissionsForAttribute(READ_PERMISSION))
            .build());

        service.truncateDefaultPermissionsForService(SERVICE_NAME, RESOURCE_TYPE);

        assertThat(countDefaultPermissions(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, ROLE_NAME, Permissions.sumOf(READ_PERMISSION)))
            .isEqualTo(0);

        assertThat(countResourceAttributes(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, SecurityClassification.PUBLIC))
            .isEqualTo(0);
    }

    @Test
    void shouldRemoveEntriesWithResourceNameFromTablesWhenEntriesExist() {
        service.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        service.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));

        service.truncateDefaultPermissionsByResourceDefinition(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);

        assertThat(countDefaultPermissions(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, ROLE_NAME, Permissions.sumOf(READ_PERMISSION)))
            .isEqualTo(0);

        assertThat(countResourceAttributes(
            SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ATTRIBUTE, SecurityClassification.PUBLIC))
            .isEqualTo(0);
    }
}
