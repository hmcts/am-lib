package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessAuditRecord;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.internal.utils.PropertyReader;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.List;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.AuditAction.GRANT;
import static uk.gov.hmcts.reform.amlib.enums.AuditAction.REVOKE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_INSERTION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_REVOKE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CHANGED_BY_NAME_FOR_INSERTION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CHANGED_BY_NAME_FOR_REVOKE;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createExplicitAccessGrantWithAudit;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createMetadata;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createMetadataForAudit;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;
import static uk.gov.hmcts.reform.amlib.internal.utils.PropertyReader.AUDIT_REQUIRED;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveImports"})
class RevokeAccessIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);

    private String resourceId;
    private String accessorId;
    private String relationship;
    private String otherRelationship;
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

        importerService.addRole(relationship = UUID.randomUUID().toString(), IDAM, PUBLIC, EXPLICIT);
        importerService.addRole(otherRelationship = UUID.randomUUID().toString(), IDAM, PUBLIC, EXPLICIT);
        importerService.addResourceDefinition(
            resourceDefinition = createResourceDefinition(serviceName, resourceType, resourceName));
    }

    @Test
    void whenRevokingResourceAccessThatDoesNotExistNoErrorExpected() {
        revokeResourceAccess(resourceId, relationship, "");

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingAccessToSpecificResourceShouldLeaveAccessToOtherResourcesUnchanged() {
        grantExplicitResourceAccess("resource-1", relationship, "");
        grantExplicitResourceAccess("resource-2", relationship, "");

        revokeResourceAccess("resource-1", relationship, "");

        assertThat(databaseHelper.countExplicitPermissions("resource-1")).isEqualTo(0);
        assertThat(databaseHelper.findExplicitPermissions("resource-2")).hasSize(1)
            .first().extracting(ExplicitAccessRecord::getAttributeAsString).isEqualTo("");
    }

    @Test
    void whenResourceNameAndServiceNameNotGivenShouldRevokeAccessBasedOnResourceIdAndResourceType() {
        ResourceDefinition resourceDefinitionWithNulls = ResourceDefinition.builder()
            .serviceName(null)
            .resourceName(null)
            .resourceType(resourceDefinition.getResourceType())
            .build();

        grantExplicitResourceAccess(resourceId, relationship, "");

        service.revokeResourceAccess(
            createMetadata(resourceId, accessorId, relationship, resourceDefinitionWithNulls, JsonPointer.valueOf(""))
        );

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Nested
    class RelationshipMatchingTests {
        @Test
        void whenRevokingResourceWithNonExistentRelationshipShouldNotRemoveAnyRecordFromDatabase() {
            grantExplicitResourceAccess(resourceId, relationship, "");

            revokeResourceAccess(resourceId, "NonExistentRelationship", "");

            assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(1)
                .extracting(ExplicitAccessRecord::getResourceId).contains(resourceId);
        }

        @Test
        void whenRevokingResourceWithNullRelationshipShouldRemoveAnyRecordFromDatabase() {
            grantExplicitResourceAccess(resourceId, relationship, "");
            grantExplicitResourceAccess(resourceId, otherRelationship, "");

            revokeResourceAccess(resourceId, null, "");

            assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
        }

        @Test
        void whenRevokingResourceWithNestedAttributeRelationshipShouldOnlyRemoveThatRelationshipAndAttribute() {
            grantExplicitResourceAccess(resourceId, relationship, "/test");
            grantExplicitResourceAccess(resourceId, relationship, "/test/nested");
            grantExplicitResourceAccess(resourceId, otherRelationship, "/test");
            grantExplicitResourceAccess(resourceId, otherRelationship, "/test/nested");

            revokeResourceAccess(resourceId, relationship, "/test");

            assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(2)
                .extracting(ExplicitAccessRecord::getAttribute).contains(JsonPointer.valueOf("/test"),
                JsonPointer.valueOf("/test/nested"));
        }

        @Test
        void whenRevokingResourceWithNullRelationshipShouldRemoveRecordFromDatabase() {
            grantExplicitResourceAccess(resourceId, null, "");
            revokeResourceAccess(resourceId, null, "");
            assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
        }


        @Test
        void whenRevokingResourceWithWildCard() {
            service.grantExplicitResourceAccess(
                createGrant(resourceId, "*", null, resourceDefinition,
                    createPermissions("", ImmutableSet.of(READ)))
            );

            service.revokeResourceAccess(
                createMetadata(resourceId, "*", null, resourceDefinition,
                    JsonPointer.valueOf(""))
            );
            assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
        }
    }

    @Nested
    class AttributeMatchingTests {
        @Test
        void whenRevokingResourceAccessOnRootShouldRemoveRecordFromDatabase() {
            grantExplicitResourceAccess(resourceId, relationship, "");
            revokeResourceAccess(resourceId, relationship, "");
            assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
        }

        @Test
        void whenPermissionRevokedFromRootShouldRemoveAllChildAttributesFromDatabase() {
            grantExplicitResourceAccess(resourceId, relationship, "/claimant");
            grantExplicitResourceAccess(resourceId, relationship, "/defendant");
            grantExplicitResourceAccess(resourceId, relationship, "/defendant/name");

            revokeResourceAccess(resourceId, relationship, "");

            assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
        }

        @Test
        void whenRevokingResourceAccessOnSingleNestedAttributeShouldRemoveAttributeAndChildAttributesFromDatabase() {
            grantExplicitResourceAccess(resourceId, relationship, "/claimant");
            grantExplicitResourceAccess(resourceId, relationship, "/claimant/name");

            revokeResourceAccess(resourceId, relationship, "/claimant");

            assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
        }

        @Test
        void whenRevokingResourceAccessOnMultipleNestedAttributeShouldRemoveAttributeAndChildAttributesFromDatabase() {
            grantExplicitResourceAccess(resourceId, relationship, "/claimant/address/city/postcode");

            revokeResourceAccess(resourceId, relationship, "/claimant/address");

            assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
        }

        @Test
        void whenRevokingAccessOnAttributeShouldRemoveOnlySpecifiedAttributeAndChildAttributesFromDatabase() {
            grantExplicitResourceAccess(resourceId, relationship, "/claimant");
            grantExplicitResourceAccess(resourceId, relationship, "/claimant/name");
            grantExplicitResourceAccess(resourceId, relationship, "/claimantAddress");

            revokeResourceAccess(resourceId, relationship, "/claimant");

            assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(1)
                .first().extracting(ExplicitAccessRecord::getAttributeAsString).isEqualTo("/claimantAddress");
        }
    }

    @Test
    void whenRevokingExplicitAccessShouldAuditRevokedRecords() {

        ExplicitAccessGrant explicitAccessGrant = createExplicitAccessGrantWithAudit(resourceId, accessorId,
            relationship, resourceDefinition, CALLING_SERVICE_NAME_FOR_INSERTION);

        service.grantExplicitResourceAccess(explicitAccessGrant);

        service.revokeResourceAccess(
            createMetadataForAudit(resourceId, accessorId, relationship, resourceDefinition, JsonPointer.valueOf(""))
        );

        List<ExplicitAccessAuditRecord> explicitAccessAuditRecord = databaseHelper
            .getExplicitAccessAuditRecords(resourceDefinition,
                "", relationship, READ.getValue());

        assertThat(explicitAccessAuditRecord).isNotNull();

        //Audit flag on
        if (TRUE.toString().equalsIgnoreCase(PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
            assertThat(explicitAccessAuditRecord.size()).isEqualTo(2);
            assertThat(explicitAccessAuditRecord.get(0).getAuditTimeStamp()).isNotNull();
            assertThat(explicitAccessAuditRecord.get(1).getAuditTimeStamp()).isNotNull();

            List<ExplicitAccessAuditRecord> expectedResult = ImmutableList.of(
                ExplicitAccessAuditRecord.builder()
                    .resourceId(resourceId)
                    .attribute(JsonPointer.valueOf(""))
                    .accessorId(accessorId)
                    .accessorType(USER)
                    .serviceName(serviceName)
                    .relationship(relationship)
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
                    .relationship(relationship)
                    .resourceName(resourceName)
                    .resourceType(resourceType)
                    .accessorType(USER)
                    .permissions(ImmutableSet.of(READ))
                    .callingServiceName(CALLING_SERVICE_NAME_FOR_REVOKE)
                    .auditTimeStamp(explicitAccessAuditRecord.get(1).getAuditTimeStamp())
                    .changedBy(CHANGED_BY_NAME_FOR_REVOKE)
                    .action(REVOKE).build());

            assertThat(explicitAccessAuditRecord).isEqualTo(expectedResult);
            assertThat(explicitAccessAuditRecord.get(1).getAuditTimeStamp()).isNotEqualTo(
                explicitAccessAuditRecord.get(0).getAuditTimeStamp());
        } else {
            assertThat(explicitAccessAuditRecord.size()).isLessThan(1);
        }
    }

    private void grantExplicitResourceAccess(String resourceId, String relationship, String attribute) {
        service.grantExplicitResourceAccess(
            createGrant(resourceId, accessorId, relationship, resourceDefinition, createPermissions(attribute,
                ImmutableSet.of(READ)))
        );
    }

    private void revokeResourceAccess(String resourceId, String relationship, String attribute) {
        service.revokeResourceAccess(
            createMetadata(resourceId, accessorId, relationship, resourceDefinition, JsonPointer.valueOf(attribute))
        );
    }
}
