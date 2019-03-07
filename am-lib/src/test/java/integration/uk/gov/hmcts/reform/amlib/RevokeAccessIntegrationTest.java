package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createMetadata;

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

        assertThat(databaseHelper.countExplicitAccessRecordsByResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    void whenRevokingResourceAccessThatDoesNotExistNoErrorExpected() {
        ams.revokeResourceAccess(createMetadata("4"));

        assertThat(databaseHelper.countExplicitAccessRecordsByResourcesById(resourceId)).isEqualTo(0);
    }
}
