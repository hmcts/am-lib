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
import uk.gov.hmcts.reform.amlib.internal.models.ResourceAttributeAudit;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessAuditRecord;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.AccessManagementAudit;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.time.Instant;
import java.util.List;
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
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_INSERTION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_UPDATES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;

@SuppressWarnings("PMD.TooManyMethods")
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
                roleName, resourceDefinition, "", ImmutableSet.of(READ), PUBLIC)))
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
    void whenAuditLogsForDefaultPermissionThenShouldReturnsAuditDetails() {
        //Add Audit
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        AccessManagementAudit audit = AccessManagementAudit.builder().lastUpdate(Instant.now())
            .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION).build();
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ), audit));
        RoleBasedAccessRecord roleBasedAccessRecord = databaseHelper.getDefaultPermissionsForAudit(resourceDefinition,
            "", roleName, READ.getValue());

        final Instant localDateTime = roleBasedAccessRecord.getAccessManagementAudit().getLastUpdate();
        assertThat(roleBasedAccessRecord).isNotNull();
        assertThat(roleBasedAccessRecord.getAccessManagementAudit().getCallingServiceName()).isNotNull();
        assertThat(roleBasedAccessRecord.getAccessManagementAudit().getCallingServiceName())
            .isEqualTo(CALLING_SERVICE_NAME_FOR_INSERTION);
        assertThat(localDateTime).isNotNull();

        //Update Audit
        audit = AccessManagementAudit.builder().lastUpdate(Instant.now())
            .callingServiceName(CALLING_SERVICE_NAME_FOR_UPDATES).build();
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ), audit));
        roleBasedAccessRecord = databaseHelper.getDefaultPermissionsForAudit(resourceDefinition, "",
            roleName, READ.getValue());
        assertThat(roleBasedAccessRecord).isNotNull();
        assertThat(roleBasedAccessRecord.getAccessManagementAudit().getCallingServiceName()).isNotNull();
        assertThat(roleBasedAccessRecord.getAccessManagementAudit().getCallingServiceName())
            .isEqualTo(CALLING_SERVICE_NAME_FOR_UPDATES);
        assertThat(roleBasedAccessRecord.getAccessManagementAudit().getLastUpdate()).isNotNull();
        assertThat(roleBasedAccessRecord.getAccessManagementAudit().getLastUpdate()).isNotEqualTo(localDateTime);
    }

    @Test
    void whenAddedDefaultPermissionsShouldAuditDefaultPermissionsAndAttributesRecords() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        AccessManagementAudit audit = AccessManagementAudit.builder().lastUpdate(Instant.now())
            .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION).build();
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ), audit));
        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());

        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isEqualTo(1);

        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);
        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isEqualTo(1);
    }

    @Test
    void whenUpdatedDefaultPermissionsShouldAuditDefaultDefaultPermissionsAndAttributesRecords() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        AccessManagementAudit audit = AccessManagementAudit.builder().lastUpdate(Instant.now())
            .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION).build();

        //Add permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ), audit));

        //Update Permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ), audit));

        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());

        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isEqualTo(2);

        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);

        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isEqualTo(2);
    }

    @Test
    void whenTruncateDefaultPermissionsAndAttributesByServiceShouldAuditDefaultPermissionsAndAttributesRecords() {

        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        AccessManagementAudit audit = AccessManagementAudit.builder().lastUpdate(Instant.now())
            .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION).build();

        //Add permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ), audit));

        ResourceDefinition resourceDefinition1;

        service.addResourceDefinition(
            resourceDefinition1 = createResourceDefinition(serviceName, resourceType, UUID.randomUUID().toString()));

        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition1, ImmutableSet.of(READ), audit));

        //truncate Permissions & attributes
        service.truncateDefaultPermissionsForService(serviceName, resourceType);

        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());

        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isEqualTo(2);

        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);

        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isEqualTo(1);

        resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition1, "", PUBLIC);

        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isEqualTo(1);
    }

    @Test
    void whenTruncateDefaultPermissionsAndAttributesByResourceDefinitionShouldAuditDefaultPermissionsAndAttrRecords() {

        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        AccessManagementAudit audit = AccessManagementAudit.builder().lastUpdate(Instant.now())
            .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION).build();

        //Add permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ), audit));

        //truncate Permissions & attributes
        service.truncateDefaultPermissionsByResourceDefinition(resourceDefinition);

        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());

        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isEqualTo(1);

        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);

        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isEqualTo(1);
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

    private DefaultPermissionGrant grantDefaultPermissionForResourceWithAudit(String roleName,
                                                                              ResourceDefinition resourceDefinition,
                                                                              Set<Permission> permissions,
                                                                              AccessManagementAudit audit) {
        return DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissionsForAttribute(JsonPointer.valueOf(""), permissions, PUBLIC))
            .accessManagementAudit(audit)
            .build();
    }
}
