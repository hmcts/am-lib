package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESS_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SECURITY_CLASSIFICATION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createMetadata;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;

@SuppressWarnings("PMD")
// AvoidDuplicateLiterals: multiple occurrences of same string literal needed for testing purposes.
class RevokeAccessIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private String resourceId;
    private static AccessManagementService ams;

    @BeforeAll
    static void setUp() {
        ams = new AccessManagementService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
    }

    @BeforeEach
    void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }


    @Test
    void whenRevokingResourceAccessShouldRemoveFromDatabase() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, READ_PERMISSION));
        ams.revokeResourceAccess(createMetadata(resourceId));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessOnRootShouldRemoveFromDatabase() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, READ_PERMISSION));
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
        assertThat(databaseHelper.countExplicitPermissions("resource2")).isEqualTo(1);
        assertThat(databaseHelper.getAttributeForExplicitAccessRecord("resource2")).isEqualTo(JsonPointer.valueOf("/claimant/name"));
    }

    @Test
    void whenRevokingAccessOnAttributeShouldRemoveOnlySpecifiedAttributeAndChildren() {
        grantExplicitResourceAccess(resourceId, "/claimant");
        grantExplicitResourceAccess(resourceId, "/claimant/name");
        grantExplicitResourceAccess(resourceId, "/claimantAddress");

        revokeResourceAccess("/claimant");

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
        assertThat(databaseHelper.getAttributeForExplicitAccessRecord(resourceId)).isEqualTo(JsonPointer.valueOf("/claimantAddress"));
    }

    @Test
    void whenRevokingResourceAccessThatDoesNotExistNoErrorExpected() {
        ams.revokeResourceAccess(createMetadata(resourceId));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(0);
    }

    private void grantExplicitResourceAccess(String resourceId, String attribute) {
        ams.grantExplicitResourceAccess(ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(createPermissions(attribute, READ_PERMISSION))
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());
    }

    private void revokeResourceAccess(String attribute) {
        ams.revokeResourceAccess(ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute(JsonPointer.valueOf(attribute))
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());
    }
}
