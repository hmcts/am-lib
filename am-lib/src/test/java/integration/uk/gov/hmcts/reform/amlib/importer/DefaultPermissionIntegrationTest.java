package integration.uk.gov.hmcts.reform.amlib.importer;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.models.ResourceAttributeAudit;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessAuditRecord;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.utils.AuditEnabled;
import uk.gov.hmcts.reform.amlib.internal.utils.AuditFlagValidate;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.AuditAction.GRANT;
import static uk.gov.hmcts.reform.amlib.enums.AuditAction.REVOKE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.RESOURCE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_INSERTION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_REVOKE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_UPDATES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CHANGED_BY_NAME_FOR_INSERTION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CHANGED_BY_NAME_FOR_REVOKE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CHANGED_BY_NAME_FOR_UPDATE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;

@SuppressWarnings({"PMD.TooManyMethods","PMD.ExcessiveImports","PMD.AvoidDuplicateLiterals"})
class DefaultPermissionIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportService.class);
    private String serviceName;
    private String resourceType;
    private String roleName;
    private ResourceDefinition resourceDefinition;
    private String resourceName;

    @BeforeEach
    void setUp() {
        serviceName = UUID.randomUUID().toString();
        resourceType = UUID.randomUUID().toString();
        roleName = UUID.randomUUID().toString();
        resourceName = UUID.randomUUID().toString();
        service.addService(serviceName);
        service.addResourceDefinition(
            resourceDefinition = createResourceDefinition(serviceName, resourceType, resourceName));
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
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_INSERTION, null));

        RoleBasedAccessRecord roleBasedAccessRecord = databaseHelper.getDefaultPermissionsForAudit(resourceDefinition,
            "", roleName, READ.getValue());

        final Instant localDateTime = roleBasedAccessRecord.getLastUpdate();
        assertThat(roleBasedAccessRecord).isNotNull();
        assertThat(roleBasedAccessRecord.getCallingServiceName()).isNotNull();
        assertThat(roleBasedAccessRecord.getCallingServiceName())
            .isEqualTo(CALLING_SERVICE_NAME_FOR_INSERTION);
        assertThat(localDateTime).isNotNull();

        //Update Audit
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_UPDATES, null));

        roleBasedAccessRecord = databaseHelper.getDefaultPermissionsForAudit(resourceDefinition, "",
            roleName, READ.getValue());
        assertThat(roleBasedAccessRecord).isNotNull();
        assertThat(roleBasedAccessRecord.getCallingServiceName()).isNotNull();
        assertThat(roleBasedAccessRecord.getCallingServiceName())
            .isEqualTo(CALLING_SERVICE_NAME_FOR_UPDATES);
        assertThat(roleBasedAccessRecord.getLastUpdate()).isNotNull();
        assertThat(roleBasedAccessRecord.getLastUpdate()).isNotEqualTo(localDateTime);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("true")
    void whenAddedDefaultPermissionsShouldAuditDefaultPermissionsAndAttributesRecords() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_INSERTION, CHANGED_BY_NAME_FOR_INSERTION));

        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());
        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isEqualTo(1);
        assertThat(accessAuditRecords.get(0).getAuditTimeStamp()).isNotNull();

        List<RoleBasedAccessAuditRecord> expectedResult = ImmutableList.of(
            RoleBasedAccessAuditRecord.builder()
                .roleName(roleName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
                .auditTimeStamp(accessAuditRecords.get(0).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT)
                .permissions(ImmutableSet.of(READ)).build());

        assertThat(expectedResult).isEqualTo(accessAuditRecords);
        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);
        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isEqualTo(1);
        assertThat(resourceAttributeAudits.get(0).getAuditTimeStamp()).isNotNull();

        List<ResourceAttributeAudit> expectedResourceAuditResult = ImmutableList.of(
            ResourceAttributeAudit.builder()
                .resourceName(resourceName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceType(resourceType)
                .defaultSecurityClassification(PUBLIC)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
                .auditTimeStamp(accessAuditRecords.get(0).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT)
                .build());

        assertThat(expectedResourceAuditResult).isEqualTo(resourceAttributeAudits);
    }


    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("false")
    void whenAddedDefaultPermissionsWithoutAuditShouldNotAuditDefaultPermissionsAndAttributesRecords() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_INSERTION, CHANGED_BY_NAME_FOR_INSERTION));

        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());
        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isLessThan(1);
        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);
        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isLessThan(1);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("true")
    void whenUpdatedDefaultPermissionsShouldAuditDefaultPermissionsAndAttributesRecords() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);

        //Add permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_INSERTION, CHANGED_BY_NAME_FOR_INSERTION));

        //Update Permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_UPDATES, CHANGED_BY_NAME_FOR_UPDATE));

        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());

        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isEqualTo(2);
        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isEqualTo(2);
        assertThat(accessAuditRecords.get(0).getAuditTimeStamp()).isNotNull();
        assertThat(accessAuditRecords.get(1).getAuditTimeStamp()).isNotNull();

        List<RoleBasedAccessAuditRecord> expectedResult = ImmutableList.of(
            RoleBasedAccessAuditRecord.builder()
                .roleName(roleName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
                .auditTimeStamp(accessAuditRecords.get(0).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT)
                .permissions(ImmutableSet.of(READ)).build(),
            RoleBasedAccessAuditRecord.builder()
                .roleName(roleName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_UPDATES)
                .auditTimeStamp(accessAuditRecords.get(1).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_UPDATE)
                .action(GRANT)
                .permissions(ImmutableSet.of(READ)).build());
        assertThat(accessAuditRecords).isEqualTo(expectedResult);

        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);

        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isEqualTo(2);
        assertThat(resourceAttributeAudits.get(0).getAuditTimeStamp()).isNotNull();
        assertThat(resourceAttributeAudits.get(1).getAuditTimeStamp()).isNotNull();

        List<ResourceAttributeAudit> expectedResourceAuditResult = ImmutableList.of(
            ResourceAttributeAudit.builder()
                .resourceName(resourceName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceType(resourceType)
                .defaultSecurityClassification(PUBLIC)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
                .auditTimeStamp(accessAuditRecords.get(0).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT)
                .build(),
            ResourceAttributeAudit.builder().resourceName(resourceName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceType(resourceType)
                .defaultSecurityClassification(PUBLIC)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_UPDATES)
                .auditTimeStamp(accessAuditRecords.get(1).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_UPDATE)
                .action(GRANT)
                .build());
        assertThat(resourceAttributeAudits).isEqualTo(expectedResourceAuditResult);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("false")
    void whenUpdatedDefaultPermissionsWithoutAuditShouldNotAuditDefaultPermissionsAndAttributesRecords() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);

        //Add permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_INSERTION, CHANGED_BY_NAME_FOR_INSERTION));

        //Update Permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_UPDATES, CHANGED_BY_NAME_FOR_UPDATE));

        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());

        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isLessThan(1);

        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);
        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isLessThan(1);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("true")
    void whenTruncateDefaultPermissionsAndAttributesByServiceShouldAuditDefaultPermissionsAndAttributesRecords() {

        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        //Add permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_INSERTION, CHANGED_BY_NAME_FOR_INSERTION));

        //truncate Permissions & attributes
        service.truncateDefaultPermissionsForService(serviceName, resourceType, CALLING_SERVICE_NAME_FOR_REVOKE,
            CHANGED_BY_NAME_FOR_REVOKE);

        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());

        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isEqualTo(2);
        assertThat(accessAuditRecords.get(0).getAuditTimeStamp()).isNotNull();
        assertThat(accessAuditRecords.get(1).getAuditTimeStamp()).isNotNull();

        List<RoleBasedAccessAuditRecord> expectedResult = ImmutableList.of(
            RoleBasedAccessAuditRecord.builder()
                .roleName(roleName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
                .auditTimeStamp(accessAuditRecords.get(0).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT)
                .permissions(ImmutableSet.of(READ)).build(),
            RoleBasedAccessAuditRecord.builder()
                .roleName(roleName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_REVOKE)
                .auditTimeStamp(accessAuditRecords.get(1).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_REVOKE)
                .action(REVOKE)
                .permissions(ImmutableSet.of(READ)).build());
        assertThat(accessAuditRecords).isEqualTo(expectedResult);

        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);

        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isEqualTo(2);
        assertThat(resourceAttributeAudits.get(0).getAuditTimeStamp()).isNotNull();
        assertThat(resourceAttributeAudits.get(1).getAuditTimeStamp()).isNotNull();

        List<ResourceAttributeAudit> expectedResourceAuditResult = ImmutableList.of(
            ResourceAttributeAudit.builder()
                .resourceName(resourceName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceType(resourceType)
                .defaultSecurityClassification(PUBLIC)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
                .auditTimeStamp(accessAuditRecords.get(0).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT)
                .build(),
            ResourceAttributeAudit.builder().resourceName(resourceName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceType(resourceType)
                .defaultSecurityClassification(PUBLIC)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_REVOKE)
                .auditTimeStamp(accessAuditRecords.get(1).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_REVOKE)
                .action(REVOKE)
                .build());
        assertThat(resourceAttributeAudits).isEqualTo(expectedResourceAuditResult);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("false")
    void whenTruncateDefaultPermissionsAndAttributesByServiceShouldWithoutAuditShouldNotAudit() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        //Add permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_INSERTION, CHANGED_BY_NAME_FOR_INSERTION));

        //truncate Permissions & attributes
        service.truncateDefaultPermissionsForService(serviceName, resourceType, CALLING_SERVICE_NAME_FOR_REVOKE,
            CHANGED_BY_NAME_FOR_REVOKE);

        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());

        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isLessThan(1);
        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);
        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isLessThan(1);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("true")
    void whenTruncateDefaultPermissionsAndAttributesByResourceDefinitionShouldAuditDefaultPermissionsAndAttrRecords() {

        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        //Add permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_INSERTION, CHANGED_BY_NAME_FOR_INSERTION));

        //truncate Permissions & attributes
        service.truncateDefaultPermissionsByResourceDefinition(resourceDefinition, CALLING_SERVICE_NAME_FOR_REVOKE,
            CHANGED_BY_NAME_FOR_REVOKE);


        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());

        assertThat(accessAuditRecords).isNotNull();

        assertThat(accessAuditRecords.size()).isEqualTo(2);
        assertThat(accessAuditRecords.get(0).getAuditTimeStamp()).isNotNull();
        assertThat(accessAuditRecords.get(1).getAuditTimeStamp()).isNotNull();

        List<RoleBasedAccessAuditRecord> expectedResult = ImmutableList.of(
            RoleBasedAccessAuditRecord.builder()
                .roleName(roleName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
                .auditTimeStamp(accessAuditRecords.get(0).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT)
                .permissions(ImmutableSet.of(READ)).build(),
            RoleBasedAccessAuditRecord.builder()
                .roleName(roleName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_REVOKE)
                .auditTimeStamp(accessAuditRecords.get(1).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_REVOKE)
                .action(REVOKE)
                .permissions(ImmutableSet.of(READ)).build());
        assertThat(accessAuditRecords).isEqualTo(expectedResult);

        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);

        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isEqualTo(2);
        assertThat(resourceAttributeAudits.get(0).getAuditTimeStamp()).isNotNull();
        assertThat(resourceAttributeAudits.get(1).getAuditTimeStamp()).isNotNull();

        List<ResourceAttributeAudit> expectedResourceAuditResult = ImmutableList.of(
            ResourceAttributeAudit.builder()
                .resourceName(resourceName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceType(resourceType)
                .defaultSecurityClassification(PUBLIC)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
                .auditTimeStamp(accessAuditRecords.get(0).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT)
                .build(),
            ResourceAttributeAudit.builder().resourceName(resourceName)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .resourceType(resourceType)
                .defaultSecurityClassification(PUBLIC)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_REVOKE)
                .auditTimeStamp(accessAuditRecords.get(1).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_REVOKE)
                .action(REVOKE)
                .build());
        assertThat(resourceAttributeAudits).isEqualTo(expectedResourceAuditResult);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("false")
    void whenTruncateDefaultPermissionsAndAttributesByResourceDefinitionWithoutAuditShouldNotAudit() {

        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        //Add permissions
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_INSERTION, CHANGED_BY_NAME_FOR_INSERTION));

        //truncate Permissions & attributes
        service.truncateDefaultPermissionsByResourceDefinition(resourceDefinition, CALLING_SERVICE_NAME_FOR_REVOKE,
            CHANGED_BY_NAME_FOR_REVOKE);


        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());

        assertThat(accessAuditRecords).isNotNull();
        assertThat(accessAuditRecords.size()).isLessThan(1);
        List<ResourceAttributeAudit> resourceAttributeAudits = databaseHelper.getResourceAttributeAuditRecords(
            resourceDefinition, "", PUBLIC);
        assertThat(resourceAttributeAudits).isNotNull();
        assertThat(resourceAttributeAudits.size()).isLessThan(1);
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

        service.truncateDefaultPermissionsForService(serviceName, resourceType, null, null);

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

        service.truncateDefaultPermissionsByResourceDefinition(resourceDefinition, null, null);

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
                                                                              String callingServiceName,
                                                                              String changedBy) {
        return DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissionsForAttribute(JsonPointer.valueOf(""), permissions, PUBLIC))
            .callingServiceName(callingServiceName)
            .changedBy(changedBy)
            .build();
    }
}
