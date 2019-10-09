package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportServiceImpl;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessAuditRecord;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.utils.AuditEnabled;
import uk.gov.hmcts.reform.amlib.internal.utils.AuditFlagValidate;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.service.DefaultRoleSetupImportService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.DEFAULT;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.AuditAction.GRANT;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_INSERTION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_UPDATES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CHANGED_BY_NAME_FOR_INSERTION;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createExplicitAccessGrantWithAudit;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForAccessorType;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class GrantAccessIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportServiceImpl.class);
    private String resourceId;
    private String accessorId;
    private String roleName;
    private ResourceDefinition resourceDefinition;
    private String resourceType;
    private String resourceName;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();
        resourceType = UUID.randomUUID().toString();
        resourceName = UUID.randomUUID().toString();

        MDC.put("caller", "Administrator");
        importerService.addRole(roleName = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        importerService.addResourceDefinition(resourceDefinition = createResourceDefinition(
            serviceName, resourceType, resourceName));
    }

    @Test
    void whenCreatingResourceAccessResourceAccessAppearsInDatabase() {

        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, roleName, resourceDefinition, ImmutableSet.of(READ)));
        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }

    @Test
    void whenCreatingResourceAccessMultipleEntriesAppearInDatabase() {
        Map<JsonPointer, Set<Permission>> multipleAttributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), ImmutableSet.of(CREATE, READ, UPDATE),
            JsonPointer.valueOf("/name"), ImmutableSet.of(CREATE, READ, UPDATE));

        service.grantExplicitResourceAccess(createGrant(
            resourceId, accessorId, roleName, resourceDefinition, multipleAttributePermissions));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(2);
    }

    @Test
    void whenCreatingDuplicateResourceAccessEntryIsOverwritten() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, roleName, resourceDefinition, ImmutableSet.of(READ)));
        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, roleName, resourceDefinition, ImmutableSet.of(READ)));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }

    @Test
    void whenAuditLogsForExplicitAccessThenShouldReturnsAuditDetails() {
        //Add Audit
        ExplicitAccessGrant explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId, roleName,
            resourceDefinition, CALLING_SERVICE_NAME_FOR_INSERTION);
        service.grantExplicitResourceAccess(explicitAccessGrant);
        ExplicitAccessRecord explicitAccessRecord = databaseHelper.getExplicitAccessRecordsForAudit(resourceDefinition,
            "", roleName, READ.getValue());
        final Instant localDateTime = explicitAccessRecord.getLastUpdate();
        assertThat(explicitAccessRecord).isNotNull();
        assertThat(explicitAccessRecord.getCallingServiceName()).isNotNull();
        assertThat(localDateTime).isNotNull();
        assertThat(explicitAccessRecord.getCallingServiceName())
            .isEqualTo(CALLING_SERVICE_NAME_FOR_INSERTION);

        //Update Audit
        explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId, roleName,
            resourceDefinition, CALLING_SERVICE_NAME_FOR_UPDATES);
        service.grantExplicitResourceAccess(explicitAccessGrant);
        explicitAccessRecord = databaseHelper.getExplicitAccessRecordsForAudit(resourceDefinition,
            "", roleName, READ.getValue());
        assertThat(explicitAccessRecord.getLastUpdate()).isNotEqualTo(localDateTime);
        assertThat(explicitAccessRecord.getCallingServiceName())
            .isEqualTo(CALLING_SERVICE_NAME_FOR_UPDATES);
        assertThat(explicitAccessRecord.getCallingServiceName()).isNotEqualTo(localDateTime);
        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("true")
    void whenGrantExplicitAccessShouldAuditGrantedRecords() {

        ExplicitAccessGrant explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId, roleName,
            resourceDefinition, CALLING_SERVICE_NAME_FOR_INSERTION);

        service.grantExplicitResourceAccess(explicitAccessGrant);

        List<ExplicitAccessAuditRecord> explicitAccessAuditRecord = databaseHelper
            .getExplicitAccessAuditRecords(resourceDefinition,
                "", roleName, READ.getValue());

        assertThat(explicitAccessAuditRecord).isNotNull();

        List<ExplicitAccessAuditRecord> expectedResult = ImmutableList.of(
            ExplicitAccessAuditRecord.builder()
                .resourceId(resourceId)
                .attribute(JsonPointer.valueOf(""))
                .accessorId(accessorId)
                .accessorType(USER)
                .serviceName(serviceName)
                .relationship(roleName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
                .permissions(ImmutableSet.of(READ))
                .auditTimeStamp(explicitAccessAuditRecord.get(0).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT).build());
        assertThat(explicitAccessAuditRecord).isEqualTo(expectedResult);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("false")
    void whenGrantExplicitAccessGrantedRecordsWithAuditDisabled() {
        ExplicitAccessGrant explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId, roleName,
            resourceDefinition, CALLING_SERVICE_NAME_FOR_INSERTION);

        service.grantExplicitResourceAccess(explicitAccessGrant);

        List<ExplicitAccessAuditRecord> explicitAccessAuditRecord = databaseHelper
            .getExplicitAccessAuditRecords(resourceDefinition,
                "", roleName, READ.getValue());

        assertThat(explicitAccessAuditRecord).isNotNull();
        assertThat(explicitAccessAuditRecord.size()).isLessThan(1);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("true")
    void whenGrantExplicitAccessWithUpdatesShouldAuditUpdatedRecords() {

        ExplicitAccessGrant explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId, roleName,
            resourceDefinition, CALLING_SERVICE_NAME_FOR_INSERTION);

        service.grantExplicitResourceAccess(explicitAccessGrant);

        explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId, roleName,
            resourceDefinition, CALLING_SERVICE_NAME_FOR_UPDATES);

        service.grantExplicitResourceAccess(explicitAccessGrant);

        List<ExplicitAccessAuditRecord> explicitAccessAuditRecord = databaseHelper
            .getExplicitAccessAuditRecords(resourceDefinition,
                "", roleName, READ.getValue());

        assertThat(explicitAccessAuditRecord).isNotNull();

        //Audit flag on
        List<ExplicitAccessAuditRecord> expectedResult = ImmutableList.of(
            ExplicitAccessAuditRecord.builder()
                .resourceId(resourceId)
                .attribute(JsonPointer.valueOf(""))
                .accessorId(accessorId)
                .accessorType(USER)
                .serviceName(serviceName)
                .relationship(roleName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
                .permissions(ImmutableSet.of(READ))
                .auditTimeStamp(explicitAccessAuditRecord.get(0).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT).build(),
            ExplicitAccessAuditRecord.builder()
                .resourceId(resourceId)
                .accessorId(accessorId)
                .attribute(JsonPointer.valueOf(""))
                .serviceName(serviceName)
                .relationship(roleName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .accessorType(USER)
                .permissions(ImmutableSet.of(READ))
                .callingServiceName(CALLING_SERVICE_NAME_FOR_UPDATES)
                .auditTimeStamp(explicitAccessAuditRecord.get(1).getAuditTimeStamp())
                .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
                .action(GRANT).build());

        assertThat(explicitAccessAuditRecord.get(0).getAuditTimeStamp()).isNotNull();
        assertThat(explicitAccessAuditRecord.get(1).getAuditTimeStamp()).isNotNull();
        assertThat(explicitAccessAuditRecord.get(1).getAuditTimeStamp())
            .isNotEqualTo(explicitAccessAuditRecord.get(0).getAuditTimeStamp());
        assertThat(explicitAccessAuditRecord).isEqualTo(expectedResult);
    }

    @Test
    @ExtendWith(AuditFlagValidate.class)
    @AuditEnabled("false")
    void whenGrantExplicitAccessWithUpdateWithoutAudit() {

        ExplicitAccessGrant explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId, roleName,
            resourceDefinition, CALLING_SERVICE_NAME_FOR_INSERTION);

        service.grantExplicitResourceAccess(explicitAccessGrant);

        explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId, roleName,
            resourceDefinition, CALLING_SERVICE_NAME_FOR_UPDATES);

        service.grantExplicitResourceAccess(explicitAccessGrant);

        List<ExplicitAccessAuditRecord> explicitAccessAuditRecord = databaseHelper
            .getExplicitAccessAuditRecords(resourceDefinition,
                "", roleName, READ.getValue());

        assertThat(explicitAccessAuditRecord).isNotNull();
        assertThat(explicitAccessAuditRecord.size()).isLessThan(1);
    }

    @Test
    void createExplicitAccessWithWildCard() {
        Map<JsonPointer, Set<Permission>> multipleAttributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), ImmutableSet.of(CREATE, READ, UPDATE),
            JsonPointer.valueOf("/name"), ImmutableSet.of(CREATE, READ, UPDATE));

        service.grantExplicitResourceAccess(createGrantForAccessorType(
            resourceId, "*", roleName, resourceDefinition, multipleAttributePermissions, DEFAULT));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(2);
    }

    @Test
    void whenCreatingResourceForMultipleUsersShouldAppearInDatabase() {
        service.grantExplicitResourceAccess(
            createPermissionsForResourceForMultipleUsers(resourceId, ImmutableSet.of("User1", "User2")));

        assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(2)
            .extracting(ExplicitAccessRecord::getAccessorId).containsOnly("User1", "User2");
    }

    @Test
    void whenCreatingResourceWithInvalidRelationshipShouldThrowPersistenceException() {
        ExplicitAccessGrant nonExistingRole = createGrant(resourceId, accessorId, "NonExistingRoleName",
            resourceDefinition, createPermissions("", ImmutableSet.of(CREATE, READ, UPDATE)));

        assertThatExceptionOfType(PersistenceException.class)
            .isThrownBy(() -> service.grantExplicitResourceAccess(nonExistingRole))
            .withMessageContaining("(relationship)=(NonExistingRoleName) is not present in table \"roles\"");
    }

    private ExplicitAccessGrant createPermissionsForResourceForMultipleUsers(String resourceId,
                                                                             Set<String> accessorIds) {
        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(accessorIds)
            .accessorType(USER)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissions("", ImmutableSet.of(READ)))
            .relationship(roleName)
            .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
            .build();
    }

    @Test
    void validateCreatingExplicitAccessWithNullRelationship() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, null, resourceDefinition, ImmutableSet.of(READ)));
        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
        assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(1)
            .extracting(ExplicitAccessRecord::getRelationship).first().isNull();
    }
}
