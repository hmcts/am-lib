package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static integration.uk.gov.hmcts.reform.amlib.TestConstants.ACCESSOR_ID;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.OTHER_ACCESSOR_ID;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.createRecord;
import static org.assertj.core.api.Assertions.assertThat;

public class GetAccessorsListIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void whenCheckingAccess_ifUserHasAccess_ShouldReturnUserIds() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));
        ams.createResourceAccess(createRecord(resourceId, OTHER_ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).containsExactly(ACCESSOR_ID, OTHER_ACCESSOR_ID);
    }

    @Test
    public void whenCheckingAccess_ifUserHasNoAccess_ShouldReturnNull() {
        ams.createResourceAccess(createRecord(resourceId, OTHER_ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).isNull();
    }

    @Test
    public void whenCheckingAccess_ToNonExistingResource_ShouldReturnNull() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

        String nonExistingResourceId = "bbbbbbbb";

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, nonExistingResourceId);

        assertThat(list).isNull();
    }
}
