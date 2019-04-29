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
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.*;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.*;

@SuppressWarnings("LineLength")
class DefaultPermissionIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportService.class);
    private String serviceName;
    private String resourceType;
    private ResourceDefinition resourceDefinition;

    @BeforeEach
    void setUp() {
        serviceName = UUID.randomUUID().toString();
        resourceType = UUID.randomUUID().toString();
        service.addService(serviceName);
        service.addResourceDefinition(
            resourceDefinition = createResourceDefinition(serviceName, resourceType, RESOURCE_NAME));
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

        assertThat(databaseHelper.countDefaultPermissions(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(1);

        assertThat(databaseHelper.getResourceAttribute(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNotNull();
    }

    @Test
    void shouldOverwriteExistingRecordWhenEntryIsAddedASecondTime() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ImmutableSet.of(READ)));
        service.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ImmutableSet.of(CREATE)));

        assertThat(databaseHelper.countDefaultPermissions(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(ImmutableSet.of(CREATE)))).isEqualTo(1);

        assertThat(databaseHelper.getResourceAttribute(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNotNull();
    }

    @Test
    void shouldRemoveAllEntriesFromTablesWhenValuesExist() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.addResourceDefinition(resourceDefinition);
        service.addResourceDefinition(createResourceDefinition(serviceName, resourceType, RESOURCE_NAME + 2));

        service.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ImmutableSet.of(READ)));
        service.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceType(resourceType)
                .resourceName(RESOURCE_NAME + 2)
                .build())
            .attributePermissions(createPermissionsForAttribute(ROOT_ATTRIBUTE, ImmutableSet.of(READ), PUBLIC))
            .build());

        service.truncateDefaultPermissionsForService(serviceName, resourceType);

        assertThat(databaseHelper.countDefaultPermissions(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(0);

        assertThat(databaseHelper.getResourceAttribute(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNull();
    }

    @Test
    void shouldRemoveEntriesWithResourceNameFromTablesWhenEntriesExist() {
        service.addRole(ROLE_NAME, RESOURCE, PUBLIC, ROLE_BASED);
        service.addResourceDefinition(resourceDefinition);
        service.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ImmutableSet.of(READ)));

        service.truncateDefaultPermissionsByResourceDefinition(resourceDefinition);

        assertThat(databaseHelper.countDefaultPermissions(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), ROLE_NAME, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(0);

        assertThat(databaseHelper.getResourceAttribute(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNull();
    }
}
