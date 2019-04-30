package integration.uk.gov.hmcts.reform.amlib.importer;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Set;
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
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;

class DefaultPermissionIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportService.class);
    private String serviceName;
    private String resourceType;
    private String roleName;
    private ResourceDefinition resourceDefinition;

    @BeforeEach
    void setUp() {
        serviceName = UUID.randomUUID().toString();
        resourceType = UUID.randomUUID().toString();
        roleName = UUID.randomUUID().toString();
        service.addService(serviceName);
        service.addResourceDefinition(
            resourceDefinition = createResourceDefinition(serviceName, resourceType, UUID.randomUUID().toString()));
        MDC.put("caller", "Administrator");
    }

    @Test
    void shouldNotBeAbleToCreateDefaultPermissionWhenRoleDoesNotExist() {
        assertThatExceptionOfType(PersistenceException.class)
            .isThrownBy(() -> service.grantDefaultPermission(createDefaultPermissionGrant(
                roleName, resourceDefinition, "", ImmutableSet.of(READ))))
            .withMessageContaining("(role_name)=(" + roleName + ") is not present in table \"roles\"");
    }

    @Test
    void shouldAddNewEntryIntoDatabaseWhenUniqueEntry() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(
            grantDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(READ)));

        assertThat(databaseHelper.countDefaultPermissions(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), roleName, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(1);

        assertThat(databaseHelper.getResourceAttribute(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNotNull();
    }

    @Test
    void shouldOverwriteExistingRecordWhenEntryIsAddedASecondTime() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(
            grantDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(READ)));
        service.grantDefaultPermission(
            grantDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(CREATE)));

        assertThat(databaseHelper.countDefaultPermissions(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), roleName, Permissions.sumOf(ImmutableSet.of(CREATE)))).isEqualTo(1);

        assertThat(databaseHelper.getResourceAttribute(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNotNull();
    }

    @Test
    void shouldRemoveAllEntriesFromTablesWhenValuesExist() {
        String otherResourceName = UUID.randomUUID().toString();
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        service.addResourceDefinition(resourceDefinition);
        service.addResourceDefinition(createResourceDefinition(serviceName, resourceType, otherResourceName));

        service.grantDefaultPermission(
            grantDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(READ)));
        service.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceType(resourceType)
                .resourceName(otherResourceName)
                .build())
            .attributePermissions(createPermissionsForAttribute(ROOT_ATTRIBUTE, ImmutableSet.of(READ), PUBLIC))
            .build());

        service.truncateDefaultPermissionsForService(serviceName, resourceType);

        assertThat(databaseHelper.countDefaultPermissions(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), roleName, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(0);

        assertThat(databaseHelper.getResourceAttribute(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNull();
    }

    @Test
    void shouldRemoveEntriesWithResourceNameFromTablesWhenEntriesExist() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        service.addResourceDefinition(resourceDefinition);
        service.grantDefaultPermission(
            grantDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(READ)));

        service.truncateDefaultPermissionsByResourceDefinition(resourceDefinition);

        assertThat(databaseHelper.countDefaultPermissions(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), roleName, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(0);

        assertThat(databaseHelper.getResourceAttribute(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNull();
    }

    private DefaultPermissionGrant grantDefaultPermissionForResource(String roleName,
                                                                     ResourceDefinition resourceDefinition,
                                                                     Set<Permission> permissions) {
        return DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissionsForAttribute(JsonPointer.valueOf(""), permissions, PUBLIC))
            .build();
    }
}
