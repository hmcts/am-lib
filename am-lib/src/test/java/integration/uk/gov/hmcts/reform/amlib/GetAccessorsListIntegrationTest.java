package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createRecord;

class GetAccessorsListIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @BeforeEach
    void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    void whenCheckingAccess_ifUserHasAccess_ShouldReturnUserIds() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));
        ams.createResourceAccess(createRecord(resourceId, OTHER_ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).containsExactly(ACCESSOR_ID, OTHER_ACCESSOR_ID);
    }

    @Test
    void whenCheckingAccess_ifUserHasNoAccess_ShouldReturnNull() {
        ams.createResourceAccess(createRecord(resourceId, OTHER_ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).isNull();
    }

    @Test
    void whenCheckingAccess_ToNonExistingResource_ShouldReturnNull() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

        String nonExistingResourceId = "bbbbbbbb";

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, nonExistingResourceId);

        assertThat(list).isNull();
    }
}
