package integration.uk.gov.hmcts.reform.amlib;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static integration.uk.gov.hmcts.reform.amlib.TestConstants.ACCESSOR_ID;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.countResourcesById;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.createRecord;
import static org.assertj.core.api.Assertions.assertThat;

public class GrantAccessIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void createQuery_whenCreatingResourceAccess_ResourceAccessAppearsInDatabase() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

        assertThat(countResourcesById(resourceId)).isEqualTo(1);
    }
}
