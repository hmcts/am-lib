package integration.uk.gov.hmcts.reform.amlib.importer;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportServiceImpl;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.models.ResourceAttributeAudit;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessAuditRecord;
import uk.gov.hmcts.reform.amlib.internal.models.RoleBasedAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.utils.Permissions;
import uk.gov.hmcts.reform.amlib.internal.utils.PropertyReader;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.service.DefaultRoleSetupImportService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
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
import static uk.gov.hmcts.reform.amlib.internal.utils.PropertyReader.AUDIT_REQUIRED;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
class DefaultPermissionIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportServiceImpl.class);
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
            getDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(READ)));

        assertThat(databaseHelper.countDefaultPermissions(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), roleName, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(1);

        assertThat(databaseHelper.getResourceAttribute(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNotNull();
    }

    @Test
    void shouldOverwriteExistingRecordWhenEntryIsAddedASecondTime() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(
            getDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(READ)));
        service.grantDefaultPermission(
            getDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(CREATE)));

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
    void whenAddedDefaultPermissionsShouldAuditDefaultPermissionsAndAttributesRecords() {
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        service.grantDefaultPermission(
            grantDefaultPermissionForResourceWithAudit(roleName, resourceDefinition, ImmutableSet.of(READ),
                CALLING_SERVICE_NAME_FOR_INSERTION, CHANGED_BY_NAME_FOR_INSERTION));

        List<RoleBasedAccessAuditRecord> accessAuditRecords = databaseHelper.getDefaultPermissionsAuditRecords(
            resourceDefinition, "", roleName, READ.getValue());
        assertThat(accessAuditRecords).isNotNull();

        //Audit flag on
        if (TRUE.toString().equalsIgnoreCase(PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
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
        } else {
            assertThat(accessAuditRecords.size()).isLessThan(1);
        }
    }

    @Test
    void whenUpdatedDefaultPermissionsShouldAuditDefaultDefaultPermissionsAndAttributesRecords() {
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

        //Audit flag on
        if (TRUE.toString().equalsIgnoreCase(PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
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
        } else {
            assertThat(accessAuditRecords.size()).isLessThan(1);
        }
    }

    @Test
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
        //Audit flag on
        if (TRUE.toString().equalsIgnoreCase(PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
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
        } else {
            assertThat(accessAuditRecords.size()).isLessThan(1);
        }
    }

    @Test
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
        //Audit flag on
        if (TRUE.toString().equalsIgnoreCase(PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
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
        } else {
            assertThat(accessAuditRecords.size()).isLessThan(1);
        }
    }


    @Test
    void shouldRemoveAllEntriesFromTablesWhenValuesExist() {
        String otherResourceName = UUID.randomUUID().toString();
        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        service.addResourceDefinition(resourceDefinition);
        service.addResourceDefinition(createResourceDefinition(serviceName, resourceType, otherResourceName));

        service.grantDefaultPermission(
            getDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(READ)));
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
            getDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(READ)));

        service.truncateDefaultPermissionsByResourceDefinition(resourceDefinition, null, null);

        assertThat(databaseHelper.countDefaultPermissions(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), roleName, Permissions.sumOf(ImmutableSet.of(READ)))).isEqualTo(0);

        assertThat(databaseHelper.getResourceAttribute(resourceDefinition,
            ROOT_ATTRIBUTE.toString(), PUBLIC)).isNull();
    }


    @Test
    void grantResourceDefaultPermissionsShouldInsertPermissionsForCaseTypes() {

        service.addRole(roleName, RESOURCE, PUBLIC, ROLE_BASED);
        service.addResourceDefinition(resourceDefinition);

        String roleName1 = UUID.randomUUID().toString();
        service.addRole(roleName1, RESOURCE, PUBLIC, ROLE_BASED);

        ResourceDefinition resourceDefinition1;

        String serviceName1 = UUID.randomUUID().toString();
        String resourceType1 = UUID.randomUUID().toString();
        String resourceName1 = UUID.randomUUID().toString();
        service.addService(serviceName1);

        service.addResourceDefinition(
            resourceDefinition1 = createResourceDefinition(serviceName1, resourceType1, resourceName1));

        service.grantDefaultPermission(
            getDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(READ)));


        List<DefaultPermissionGrant> defaultPermissionGrants = ImmutableList.of(
            getDefaultPermissionForResource(roleName, resourceDefinition, ImmutableSet.of(READ)),
            getDefaultPermissionForResourceForAttribute(roleName, resourceDefinition, ImmutableSet.of(READ),
                "child"));

        List<DefaultPermissionGrant> defaultPermissionGrants1 = ImmutableList.of(
            getDefaultPermissionForResource(roleName1, resourceDefinition1, ImmutableSet.of(READ)),
            getDefaultPermissionForResourceForAttribute(roleName1, resourceDefinition1, ImmutableSet.of(READ),
                "child"));

        Map<String, List<DefaultPermissionGrant>> mapAccessGrant = ImmutableMap.of(resourceName,
            defaultPermissionGrants, resourceName1, defaultPermissionGrants1);
        service.grantResourceDefaultPermissions(mapAccessGrant);

        assertThat(databaseHelper.countDefaultPermissionsForBatch(resourceDefinition)).isEqualTo(2);
        assertThat(databaseHelper.countDefaultPermissionsForBatch(resourceDefinition1)).isEqualTo(2);
        assertThat(databaseHelper.countResourceAttributeForBatch(resourceDefinition)).isEqualTo(2);
        assertThat(databaseHelper.countResourceAttributeForBatch(resourceDefinition1)).isEqualTo(2);

        assertThat(databaseHelper.countDefaultPermissionsAuditForBatch(resourceDefinition)).isEqualTo(4);
        assertThat(databaseHelper.countDefaultPermissionsAuditForBatch(resourceDefinition1)).isEqualTo(2);
        assertThat(databaseHelper.countResourceAttributeAuditForBatch(resourceDefinition)).isEqualTo(4);
        assertThat(databaseHelper.countResourceAttributeAuditForBatch(resourceDefinition1)).isEqualTo(2);
    }

    private DefaultPermissionGrant getDefaultPermissionForResource(String roleName,
                                                                   ResourceDefinition resourceDefinition,
                                                                   Set<Permission> permissions) {
        return DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissionsForAttribute(JsonPointer.valueOf(""), permissions, PUBLIC))
            .build();
    }

    private DefaultPermissionGrant getDefaultPermissionForResourceForAttribute(String roleName,
                                                                               ResourceDefinition resourceDefinition,
                                                                               Set<Permission> permissions,
                                                                               String attribute) {
        return DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissionsForAttribute(JsonPointer.valueOf("/" + attribute), permissions,
                PUBLIC))
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
