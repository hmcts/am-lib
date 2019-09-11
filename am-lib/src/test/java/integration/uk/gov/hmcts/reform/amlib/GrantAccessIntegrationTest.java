package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.AccessManagementAudit;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.DEFAULT;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createAccessManagementAudit;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createExplicitAccessGrantWithAudit;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForAccessorType;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;

@SuppressWarnings("PMD.ExcessiveImports")
class GrantAccessIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
    private String resourceId;
    private String accessorId;
    private String roleName;
    private ResourceDefinition resourceDefinition;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();

        MDC.put("caller", "Administrator");
        importerService.addRole(roleName = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        importerService.addResourceDefinition(resourceDefinition = createResourceDefinition(
            serviceName, UUID.randomUUID().toString(), UUID.randomUUID().toString()));
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
        AccessManagementAudit accessManagementAudit = AccessManagementAudit.builder().lastUpdate(LocalDateTime.now())
            .callingServiceName("integration-test").build();
        ExplicitAccessGrant explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId, roleName,
            resourceDefinition, accessManagementAudit);
        service.grantExplicitResourceAccess(explicitAccessGrant);
        ExplicitAccessRecord explicitAccessRecord = databaseHelper.getExplicitAccessRecordsForAudit(resourceDefinition,
            "", roleName, READ.getValue());
        final LocalDateTime localDateTime = explicitAccessRecord.getAccessManagementAudit().getLastUpdate();
        assertThat(explicitAccessRecord).isNotNull();
        assertThat(explicitAccessRecord.getAccessManagementAudit().getCallingServiceName()).isNotNull();
        assertThat(localDateTime).isNotNull();
        assertThat(explicitAccessRecord.getAccessManagementAudit().getCallingServiceName())
            .isEqualTo("integration-test");

        //Update Audit
        accessManagementAudit = AccessManagementAudit.builder().lastUpdate(LocalDateTime.now())
            .callingServiceName("integration-test123").build();
        explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId, roleName,
            resourceDefinition, accessManagementAudit);
        service.grantExplicitResourceAccess(explicitAccessGrant);
        explicitAccessRecord = databaseHelper.getExplicitAccessRecordsForAudit(resourceDefinition,
            "", roleName, READ.getValue());
        assertThat(explicitAccessRecord.getAccessManagementAudit().getLastUpdate()).isNotEqualTo(localDateTime);
        assertThat(explicitAccessRecord.getAccessManagementAudit().getCallingServiceName())
            .isEqualTo("integration-test123");
        assertThat(explicitAccessRecord.getAccessManagementAudit().getCallingServiceName()).isNotEqualTo(localDateTime);
        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
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
            .accessManagementAudit(createAccessManagementAudit())
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
