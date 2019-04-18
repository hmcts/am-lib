package integration.uk.gov.hmcts.reform.amlib.importer;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.RESOURCE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class DefaultPermissionIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportService.class);

    @BeforeEach
    void setUp() {
        service.addService(SERVICE_NAME);
        service.addResourceDefinition(createResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME));
        MDC.put("caller", "Administrator");
    }

    @Test
    void shouldNotBeAbleToCreateDefaultPermissionWhenRoleDoesNotExist() {
        assertThatExceptionOfType(PersistenceException.class)
            .isThrownBy(() -> service.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION)))
            .withMessageContaining("(role_name)=(Solicitor) is not present in table \"roles\"");
    }

    @Test
    void shouldAddNewEntryIntoDatabaseWhenUniqueEntry() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));

        assertThat(databaseHelper.countDefaultPermissions(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(READ_PERMISSION))).isEqualTo(1);

        assertThat(databaseHelper.getResourceAttribute(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNotNull();
    }

    @Test
    void shouldOverwriteExistingRecordWhenEntryIsAddedASecondTime() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));
        service.grantDefaultPermission(createDefaultPermissionGrant(CREATE_PERMISSION));

        assertThat(databaseHelper.countDefaultPermissions(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(CREATE_PERMISSION))).isEqualTo(1);

        assertThat(databaseHelper.getResourceAttribute(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNotNull();
    }

    @Test
    void shouldRemoveAllEntriesFromTablesWhenValuesExist() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.addResourceDefinition(createResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME));
        service.addResourceDefinition(createResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME + 2));

        service.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));
        service.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(SERVICE_NAME)
                .resourceType(RESOURCE_TYPE)
                .resourceName(RESOURCE_NAME + 2)
                .build())
            .attributePermissions(createPermissionsForAttribute(ROOT_ATTRIBUTE, READ_PERMISSION, PUBLIC))
            .build());

        service.truncateDefaultPermissionsForService(SERVICE_NAME, RESOURCE_TYPE);

        assertThat(databaseHelper.countDefaultPermissions(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(READ_PERMISSION))).isEqualTo(0);

        assertThat(databaseHelper.getResourceAttribute(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNull();
    }

    @Test
    void shouldRemoveEntriesWithResourceNameFromTablesWhenEntriesExist() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.addResourceDefinition(createResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME));
        service.grantDefaultPermission(createDefaultPermissionGrant(READ_PERMISSION));

        service.truncateDefaultPermissionsByResourceDefinition(
            createResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME));

        assertThat(databaseHelper.countDefaultPermissions(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(READ_PERMISSION))).isEqualTo(0);

        assertThat(databaseHelper.getResourceAttribute(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNull();
    }
}
