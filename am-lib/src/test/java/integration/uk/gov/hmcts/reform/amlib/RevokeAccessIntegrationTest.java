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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;
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

        service.revokeResourceAccess(ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(USER)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute(JsonPointer.valueOf(""))
            .securityClassification(PUBLIC)
            .relationship("NonExistentRelationship")
            .build());

        assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(1)
            .extracting(ExplicitAccessRecord::getResourceId).contains(resourceId);
    }

    @Test
    void whenRevokingResourceWithNullRelationshipShouldRemoveAnyRecordFromDatabase() {
        grantExplicitResourceAccess(resourceId, "");

        service.revokeResourceAccess(ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(USER)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute(JsonPointer.valueOf(""))
            .securityClassification(PUBLIC)
            .relationship(null)
            .build());

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    private void grantExplicitResourceAccess(String resourceId, String attribute) {
        service.grantExplicitResourceAccess(ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(USER)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(createPermissions(attribute, READ_PERMISSION))
            .securityClassification(PUBLIC)
            .relationship(ROLE_NAME)
            .build());
    }

    private void revokeResourceAccess(String attribute) {
        service.revokeResourceAccess(ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(USER)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute(JsonPointer.valueOf(attribute))
            .securityClassification(PUBLIC)
            .relationship(ROLE_NAME)
            .build());
    }
}
