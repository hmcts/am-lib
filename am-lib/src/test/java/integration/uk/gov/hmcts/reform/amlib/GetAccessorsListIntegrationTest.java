package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.grantAccess;

public class GetAccessorsListIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void whenCheckingAccess_ifUserHasAccess_ShouldReturnUserIds() {
        Map<JsonPointer, Set<Permission>> singleAttributePermission = new ConcurrentHashMap<>();
        singleAttributePermission.put(JsonPointer.valueOf(""), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);


        ams.grantExplicitResourceAccess(grantAccess(resourceId, ACCESSOR_ID, singleAttributePermission));
        ams.grantExplicitResourceAccess(grantAccess(resourceId, OTHER_ACCESSOR_ID, singleAttributePermission));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).containsExactly(ACCESSOR_ID, OTHER_ACCESSOR_ID);
    }

    @Test
    public void whenCheckingAccess_ifUserHasNoAccess_ShouldReturnNull() {
        Map<JsonPointer, Set<Permission>> singleAttributePermission = new ConcurrentHashMap<>();
        singleAttributePermission.put(JsonPointer.valueOf(""), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);

        ams.grantExplicitResourceAccess(grantAccess(resourceId, OTHER_ACCESSOR_ID, singleAttributePermission));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).isNull();
    }

    @Test
    public void whenCheckingAccess_ToNonExistingResource_ShouldReturnNull() {
        Map<JsonPointer, Set<Permission>> singleAttributePermission = new ConcurrentHashMap<>();
        singleAttributePermission.put(JsonPointer.valueOf(""), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);

        ams.grantExplicitResourceAccess(grantAccess(resourceId, ACCESSOR_ID, singleAttributePermission));

        String nonExistingResourceId = "bbbbbbbb";

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, nonExistingResourceId);

        assertThat(list).isNull();
    }
}
