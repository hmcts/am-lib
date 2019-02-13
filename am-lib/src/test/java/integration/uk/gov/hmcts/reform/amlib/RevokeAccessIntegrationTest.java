package integration.uk.gov.hmcts.reform.amlib;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static integration.uk.gov.hmcts.reform.amlib.TestConstants.countResourcesById;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.grantAndRevokeAccessToRecord;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.removeRecord;
import static org.assertj.core.api.Assertions.assertThat;

public class RevokeAccessIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void revokeResourceAccess_whenRevokingResourceAccess_ResourceAccessRemovedFromDatabase() {
        grantAndRevokeAccessToRecord(resourceId);

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    public void revokeResourceAccess_whenRevokingResourceAccessThatDoesNotExist_NoErrorExpected() {
        ams.revokeResourceAccess(removeRecord("4"));

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }
}