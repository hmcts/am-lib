package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SINGLE_ATTRIBUTE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createRecord;

public class GetAccessorsListIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void whenCheckingAccess_ifUserHasAccess_ShouldReturnUserIds() {
        ams.grantExplicitResourceAccess(createRecord(resourceId, ACCESSOR_ID, SINGLE_ATTRIBUTE_PERMISSION));
        ams.grantExplicitResourceAccess(createRecord(resourceId, OTHER_ACCESSOR_ID, SINGLE_ATTRIBUTE_PERMISSION));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).containsExactly(ACCESSOR_ID, OTHER_ACCESSOR_ID);
    }

    @Test
    public void whenCheckingAccess_ifUserHasNoAccess_ShouldReturnNull() {
        ams.grantExplicitResourceAccess(createRecord(resourceId, OTHER_ACCESSOR_ID, SINGLE_ATTRIBUTE_PERMISSION));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).isNull();
    }

    @Test
    public void whenCheckingAccess_ToNonExistingResource_ShouldReturnNull() {
        ams.grantExplicitResourceAccess(createRecord(resourceId, ACCESSOR_ID, SINGLE_ATTRIBUTE_PERMISSION));

        String nonExistingResourceId = "bbbbbbbb";

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, nonExistingResourceId);

        assertThat(list).isNull();
    }
}
