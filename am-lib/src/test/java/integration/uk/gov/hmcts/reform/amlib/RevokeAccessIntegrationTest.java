package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createMetadata;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals", "LineLength"})
class RevokeAccessIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
    private static ResourceDefinition resourceDefinition;

    private final String relationship = ROLE_NAME;
    private final String otherRelationship = OTHER_ROLE_NAME;
    private String resourceId;
    private String accessorId;
    String serviceName;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();
        MDC.put("caller", "Administrator");
        serviceName = UUID.randomUUID().toString();
        importerService.addService(serviceName);
        importerService.addResourceDefinition(ResourceDefinition.builder()
            .serviceName(serviceName)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .build());
        resourceDefinition = createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME);
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

    private void grantExplicitResourceAccess(String resourceId, String relationship, String attribute) {
        service.grantExplicitResourceAccess(
            createGrant(resourceId, accessorId, relationship, resourceDefinition, createPermissions(attribute, ImmutableSet.of(READ)))
        );
    }

    private void revokeResourceAccess(String resourceId, String relationship, String attribute) {
        service.revokeResourceAccess(
            createMetadata(resourceId, accessorId, relationship, resourceDefinition, JsonPointer.valueOf(attribute))
        );
    }
}
