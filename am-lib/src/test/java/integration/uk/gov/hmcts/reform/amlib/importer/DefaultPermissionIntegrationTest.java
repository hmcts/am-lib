package integration.uk.gov.hmcts.reform.amlib.importer;

import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.RESOURCE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;

@SuppressWarnings("LineLength")
class DefaultPermissionIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportService.class);
    private String serviceName;
    private ResourceDefinition resourceDefinition;

    @BeforeEach
    void setUp() {
        serviceName = UUID.randomUUID().toString();
        service.addService(serviceName);
        service.addResourceDefinition(
            resourceDefinition = createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME));
        MDC.put("caller", "Administrator");
    }

    @Test
    void shouldNotBeAbleToCreateDefaultPermissionWhenRoleDoesNotExist() {
        assertThatExceptionOfType(PersistenceException.class)
            .isThrownBy(() -> service.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ImmutableSet.of(READ))))
            .withMessageContaining("(role_name)=(Solicitor) is not present in table \"roles\"");
    }

    @Test
    void shouldAddNewEntryIntoDatabaseWhenUniqueEntry() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ImmutableSet.of(READ)));

        assertThat(databaseHelper.countDefaultPermissions(serviceName, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(1);

        assertThat(databaseHelper.getResourceAttribute(serviceName, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNotNull();
    }

    @Test
    void shouldOverwriteExistingRecordWhenEntryIsAddedASecondTime() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ImmutableSet.of(READ)));
        service.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ImmutableSet.of(CREATE)));

        assertThat(databaseHelper.countDefaultPermissions(serviceName, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(ImmutableSet.of(CREATE)))).isEqualTo(1);

        assertThat(databaseHelper.getResourceAttribute(serviceName, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNotNull();
    }

    @Test
    void shouldRemoveAllEntriesFromTablesWhenValuesExist() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.addResourceDefinition(createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME));
        service.addResourceDefinition(createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME + 2));

        service.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ImmutableSet.of(READ)));
        service.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceType(RESOURCE_TYPE)
                .resourceName(RESOURCE_NAME + 2)
                .build())
            .attributePermissions(createPermissionsForAttribute(ROOT_ATTRIBUTE, ImmutableSet.of(READ), PUBLIC))
            .build());

        service.truncateDefaultPermissionsForService(serviceName, RESOURCE_TYPE);

        assertThat(databaseHelper.countDefaultPermissions(serviceName, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(0);

        assertThat(databaseHelper.getResourceAttribute(serviceName, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNull();
    }

    @Test
    void shouldRemoveEntriesWithResourceNameFromTablesWhenEntriesExist() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.addResourceDefinition(createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME));
        service.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ImmutableSet.of(READ)));

        service.truncateDefaultPermissionsByResourceDefinition(
            createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME));

        assertThat(databaseHelper.countDefaultPermissions(serviceName, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(0);

        assertThat(databaseHelper.getResourceAttribute(serviceName, RESOURCE_TYPE, RESOURCE_NAME,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNull();
    }
}
