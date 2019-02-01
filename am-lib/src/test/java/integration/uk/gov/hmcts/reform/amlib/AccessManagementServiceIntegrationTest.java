package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permissions;
import uk.gov.hmcts.reform.amlib.models.ExplicitPermissions;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class AccessManagementServiceIntegrationTest extends IntegrationBaseTest {

    private String resourceId;
    private static final String ACCESSOR_ID = "a";
    private static final String OTHER_ACCESSOR_ID = "b";

    private final JsonNode jsonObject = JsonNodeFactory.instance.objectNode();
    private ExplicitPermissions explicitReadCreateUpdatePermissions;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
        explicitReadCreateUpdatePermissions = new ExplicitPermissions(
                Permissions.CREATE, Permissions.READ, Permissions.UPDATE
        );
    }

    @Test
    public void createQuery_whenCreatingResourceAccess_ResourceAccessAppearsInDatabase() {
        ams.createResourceAccess(resourceId, "dsa", explicitReadCreateUpdatePermissions);

        int count = jdbi.open().createQuery(
                "select count(1) from access_management where resource_id = ?")
                .bind(0, resourceId)
                .mapTo(int.class)
                .findOnly();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void whenCheckingAccess_ifUserHasAccess_ShouldReturnUserIds() {
        ams.createResourceAccess(resourceId, ACCESSOR_ID, explicitReadCreateUpdatePermissions);
        ams.createResourceAccess(resourceId, OTHER_ACCESSOR_ID, explicitReadCreateUpdatePermissions);

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).containsExactly(ACCESSOR_ID, OTHER_ACCESSOR_ID);
    }

    @Test
    public void whenCheckingAccess_ifUserHasNoAccess_ShouldReturnNull() {
        ams.createResourceAccess(resourceId, "c", explicitReadCreateUpdatePermissions);
        ams.createResourceAccess(resourceId, OTHER_ACCESSOR_ID, explicitReadCreateUpdatePermissions);
        ams.createResourceAccess("otherResourceId", ACCESSOR_ID, explicitReadCreateUpdatePermissions);

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).isNull();
    }

    @Test
    public void whenCheckingAccess_ToNonExistingResource_ShouldReturnNull() {
        ams.createResourceAccess(resourceId, ACCESSOR_ID, explicitReadCreateUpdatePermissions);
        ams.createResourceAccess(resourceId, OTHER_ACCESSOR_ID, explicitReadCreateUpdatePermissions);

        String nonExistingResourceId = "bbbbbbbb";

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, nonExistingResourceId);

        assertThat(list).isNull();
    }

    @Test
    public void filterResource_whenRowExistWithAccessorIdAndResourceId_ReturnPassedJsonObject() {
        ams.createResourceAccess(resourceId, ACCESSOR_ID, explicitReadCreateUpdatePermissions);

        JsonNode result = ams.filterResource(ACCESSOR_ID, resourceId, jsonObject);

        assertThat(result).isEqualTo(jsonObject);
    }

    @Test
    public void filterResource_whenRowNotExistWithAccessorIdAndResourceId_ReturnNull() {
        ams.createResourceAccess(resourceId, ACCESSOR_ID, explicitReadCreateUpdatePermissions);
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        JsonNode result = ams.filterResource(nonExistingUserId, nonExistingResourceId, jsonObject);

        assertThat(result).isNull();
    }

    @Test
    public void filterResource_whenRowExistsAndDoesntHaveReadPermissions_ReturnNull() {
        String userId = "def";
        ams.createResourceAccess(resourceId, userId,
                new ExplicitPermissions(Permissions.CREATE, Permissions.UPDATE)
        );

        JsonNode result = ams.filterResource(userId, resourceId, jsonObject);

        assertThat(result).isNull();
    }
}
