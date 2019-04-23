package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createMetadata;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
class RevokeAccessIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private String resourceId;
    private String accessorId;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();
        MDC.put("caller", "Administrator");
    }

    @Test
    void whenRevokingResourceAccessShouldRemoveFromDatabase() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, accessorId, READ_PERMISSION));
        service.revokeResourceAccess(createMetadata(resourceId, accessorId));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessOnRootShouldRemoveFromDatabase() {
        grantExplicitResourceAccess(resourceId, "");
        revokeResourceAccess("");

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessOnSingleNestedAttributeShouldRemoveFromDatabase() {
        grantExplicitResourceAccess(resourceId, "/claimant");
        grantExplicitResourceAccess(resourceId, "/claimant/name");
        revokeResourceAccess("/claimant");

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessOnMultipleNestedAttributesShouldRemoveFromDatabase() {
        grantExplicitResourceAccess(resourceId, "/claimant/address/city/postcode");
        revokeResourceAccess("/claimant/address");

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Test
    void whenPermissionsOnlyOnChildAttributeRevokingPermissionsOnParentShouldCascade() {
        grantExplicitResourceAccess(resourceId, "/claimant/name");
        revokeResourceAccess("/claimant");

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Test
    void whenPermissionRevokedFromRootShouldDeleteAllChildAttributes() {
        grantExplicitResourceAccess(resourceId, "/claimant");
        grantExplicitResourceAccess(resourceId, "/defendant");
        grantExplicitResourceAccess(resourceId, "/defendant/name");
        revokeResourceAccess("");

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingSpecificEntryShouldRemoveCorrectEntry() {
        grantExplicitResourceAccess(resourceId, "/claimant/name");
        grantExplicitResourceAccess("resource2", "/claimant/name");
        revokeResourceAccess("/claimant/name");

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
        assertThat(databaseHelper.findExplicitPermissions("resource2")).hasSize(1)
            .first().extracting(ExplicitAccessRecord::getAttributeAsString).isEqualTo("/claimant/name");
    }

    @Test
    void whenRevokingAccessOnAttributeShouldRemoveOnlySpecifiedAttributeAndChildren() {
        grantExplicitResourceAccess(resourceId, "/claimant");
        grantExplicitResourceAccess(resourceId, "/claimant/name");
        grantExplicitResourceAccess(resourceId, "/claimantAddress");
        revokeResourceAccess("/claimant");

        assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(1)
            .first().extracting(ExplicitAccessRecord::getAttributeAsString).isEqualTo("/claimantAddress");
    }

    @Test
    void whenRevokingResourceAccessThatDoesNotExistNoErrorExpected() {
        service.revokeResourceAccess(createMetadata(resourceId, accessorId));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceWithNonExistentRelationshipShouldNotRemoveAnyRecordFromDatabase() {
        grantExplicitResourceAccess(resourceId, "");

        service.revokeResourceAccess(createMetadata(resourceId, accessorId,
            "NonExistentRelationship", JsonPointer.valueOf("")));

        assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(1)
            .extracting(ExplicitAccessRecord::getResourceId).contains(resourceId);
    }

    @Test
    void whenRevokingResourceWithNullRelationshipShouldRemoveAnyRecordFromDatabase() {
        grantExplicitResourceAccess(resourceId, "");

        service.revokeResourceAccess(createMetadata(resourceId, accessorId, null, JsonPointer.valueOf("")));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceWithNestedAttributeRelationshipShouldOnlyRemoveThatRelationshipAndAttribute() {
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, ROLE_NAME,
            createPermissions("/test", READ_PERMISSION)));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, ROLE_NAME,
            createPermissions("/test/nested", READ_PERMISSION)));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, OTHER_ROLE_NAME,
            createPermissions("/test", READ_PERMISSION)));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, OTHER_ROLE_NAME,
            createPermissions("/test/nested", READ_PERMISSION)));

        revokeResourceAccess("/test");

        assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(2)
            .extracting(ExplicitAccessRecord::getAttribute).contains(JsonPointer.valueOf("/test"),
            JsonPointer.valueOf("/test/nested"));
    }

    @Test
    void whenRevokingResourceWithOtherValidRelationshipShouldNotRemoveResourceFromDatabase() {
        grantExplicitResourceAccess(resourceId, "/test");
        grantExplicitResourceAccess(resourceId, "/test/nested");

        service.revokeResourceAccess(createMetadata(resourceId, accessorId, OTHER_ROLE_NAME, JsonPointer.valueOf("")));

        assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(2)
            .extracting(ExplicitAccessRecord::getResourceId).contains(resourceId);
    }

    private void grantExplicitResourceAccess(String resourceId, String attribute) {
        service.grantExplicitResourceAccess(ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(USER)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(SERVICE_NAME)
                .resourceType(RESOURCE_TYPE)
                .resourceName(RESOURCE_NAME)
                .build())
            .attributePermissions(createPermissions(attribute, READ_PERMISSION))
            .relationship(ROLE_NAME)
            .build());
    }

    private void revokeResourceAccess(String attribute) {
        service.revokeResourceAccess(ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(USER)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(SERVICE_NAME)
                .resourceType(RESOURCE_TYPE)
                .resourceName(RESOURCE_NAME)
                .build())
            .attribute(JsonPointer.valueOf(attribute))
            .relationship(ROLE_NAME)
            .build());
    }
}
