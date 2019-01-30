package integration.uk.gov.hmcts.reform.amlib;

import org.junit.Test;
import uk.gov.hmcts.reform.amlib.UserDetails;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class AccessManagementServiceIntegrationTest extends IntegrationBaseTest {

    private static final String resourceId = UUID.randomUUID().toString();

    @Test
    public void whenCreatingResourceAccess_ResourceAccessAppearsInDatabase() {
        ams.createResourceAccess(resourceId, "dsa");

        int count = jdbi.open().createQuery(
                "select count(1) from \"AccessManagement\" where \"resourceId\" = ?")
                .bind(0, resourceId)
                .mapTo(int.class)
                .findOnly();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void whenCheckingAccess_ifUserHasAccess_ShouldReturnUserIds() {
        ams.createResourceAccess(resourceId, "a");
        ams.createResourceAccess(resourceId, "b");

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("a");

        List<String> list = ams.checkAccess(userDetails, resourceId);

        assertThat(list).containsExactly("a", "b");
    }

    @Test
    public void whenCheckingAccess_ifUserHasNoAccess_ShouldReturnNull() {
        ams.createResourceAccess(resourceId, "c");
        ams.createResourceAccess(resourceId, "b");

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("a");

        List<String> list = ams.checkAccess(userDetails, resourceId);

        assertThat(list).isNull();
    }

    @Test
    public void whenCheckingAccess_ToNonExistingResource_ShouldReturnNull() {
        ams.createResourceAccess(resourceId, "a");
        ams.createResourceAccess(resourceId, "b");

        final String nonExistingResourceId = "bbbbbbbb";

        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("a");

        List<String> list = ams.checkAccess(userDetails, nonExistingResourceId);

        assertThat(list).isNull();
    }
}
