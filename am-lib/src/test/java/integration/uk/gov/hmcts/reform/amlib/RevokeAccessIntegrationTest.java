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
//AvoidDuplicateLiterals multiple occurences of same string literal needed for testing purposes.
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

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessOnSingleNestedAttributeShouldRemoveFromDatabase() {
        grantExplicitResourceAccess(resourceId, "/test");
        grantExplicitResourceAccess(resourceId, "/test/childTest");
        revokeResourceAccess("/test");

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessOnMultipleNestedAttributesShouldRemoveFromDatabase() {
        grantExplicitResourceAccess(resourceId, "/test/childTest/secondChild/thirdChild");
        revokeResourceAccess("/test/childTest");

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenPermissionsOnlyOnChildAttributeRevokingPermissionsOnParentShouldCascade() {
        grantExplicitResourceAccess(resourceId, "/test/childTest");
        revokeResourceAccess("/test");

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenPermissionRevokedFromRootShouldDeleteAllChildAttributes() {
        grantExplicitResourceAccess(resourceId, "/child");
        grantExplicitResourceAccess(resourceId, "/childTest");
        grantExplicitResourceAccess(resourceId, "/test/childTest");
        revokeResourceAccess("");

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingSpecificEntryShouldRemoveCorrectEntry() {
        grantExplicitResourceAccess(resourceId, "/test/childTest");
        grantExplicitResourceAccess("resource2", "/test/childTest");
        revokeResourceAccess("/test/childTest");

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
        assertThat(countResourcesById("resource2")).isEqualTo(1);
    }

    @Test
    void whenRevokingAccessOnAttributeShouldRemoveOnlySpecifiedAttributeAndChildren() {
        grantExplicitResourceAccess(resourceId, "/amount");
        grantExplicitResourceAccess(resourceId, "/amount/lastUpdated");
        grantExplicitResourceAccess(resourceId, "/amountInPounds");

        revokeResourceAccess("/amount");

        assertThat(countResourcesById(resourceId)).isEqualTo(1);
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
