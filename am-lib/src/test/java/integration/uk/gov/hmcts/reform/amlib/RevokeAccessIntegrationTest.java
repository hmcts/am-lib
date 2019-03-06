package integration.uk.gov.hmcts.reform.amlib;

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
    void whenRevokingResourceAccessResourceAccessRemovedFromDatabase() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, READ_PERMISSION));
        ams.revokeResourceAccess(createMetadata(resourceId));

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessOnRootResourceAccessRemovedFromDatabase() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, READ_PERMISSION));
        ams.revokeResourceAccess(ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("/")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessOnSingleNestedAttributeResourceAccessRemovedFromDatabase() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, READ_PERMISSION));
        ams.revokeResourceAccess(ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("/test/childTest")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessOnMultipleNestedAttributesResourceAccessRemovedFromDatabase() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, READ_PERMISSION));
        ams.revokeResourceAccess(ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("/test/childTest/secondChildTest")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenPermissionsOnlyOnChildAttributeRevokingPermissionsOnParentShouldCascade() {
        ams.grantExplicitResourceAccess(ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(createPermissions("/childTest", READ_PERMISSION))
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());
        ams.revokeResourceAccess(ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("/test/childTest/secondChildTest")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessThatDoesNotExistNoErrorExpected() {
        ams.revokeResourceAccess(createMetadata("4"));

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }
}
