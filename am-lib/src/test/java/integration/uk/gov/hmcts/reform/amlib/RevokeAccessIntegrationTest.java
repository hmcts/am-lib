package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createRecord;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.removeRecord;
import static org.assertj.core.api.Assertions.assertThat;

public class RevokeAccessIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void revokeResourceAccess_whenRevokingResourceAccess_ResourceAccessRemovedFromDatabase() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));
        ams.revokeResourceAccess(removeRecord(resourceId));

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    public void revokeResourceAccess_whenRevokingResourceAccessThatDoesNotExist_NoErrorExpected() {
        ams.revokeResourceAccess(removeRecord("4"));

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }
}