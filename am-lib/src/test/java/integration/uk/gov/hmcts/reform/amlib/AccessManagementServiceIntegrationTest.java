package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class AccessManagementServiceIntegrationTest extends IntegrationBaseTest {

    private String resourceId;
    private static final String accessorId = "a";
    private static final String otherAccessorId = "b";

    private final JsonNode jsonObject = JsonNodeFactory.instance.objectNode();

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void createQuery_whenCreatingResourceAccess_ResourceAccessAppearsInDatabase() {
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
        ams.createResourceAccess(resourceId, accessorId);
        ams.createResourceAccess(resourceId, otherAccessorId);

        List<String> list = ams.getAccessorsList(accessorId, resourceId);

        assertThat(list).containsExactly(accessorId, otherAccessorId);
    }

    @Test
    public void whenCheckingAccess_ifUserHasNoAccess_ShouldReturnNull() {
        ams.createResourceAccess(resourceId, "c");
        ams.createResourceAccess(resourceId, otherAccessorId);
        ams.createResourceAccess("otherResourceId", accessorId);

        List<String> list = ams.getAccessorsList(accessorId, resourceId);

        assertThat(list).isNull();
    }

    @Test
    public void whenCheckingAccess_ToNonExistingResource_ShouldReturnNull() {
        ams.createResourceAccess(resourceId, accessorId);
        ams.createResourceAccess(resourceId, otherAccessorId);

        final String nonExistingResourceId = "bbbbbbbb";

        List<String> list = ams.getAccessorsList(accessorId, nonExistingResourceId);

        assertThat(list).isNull();
    }

    @Test
    public void filterResource_whenRowExistWithAccessorIdAndResourceId_ReturnPassedJsonObject() {
        ams.createResourceAccess(resourceId, accessorId);

        JsonNode result = ams.filterResource(accessorId, resourceId, jsonObject);

        assertThat(result).isEqualTo(jsonObject);
    }

    @Test
    public void filterResource_whenRowNotExistWithAccessorIdAndResourceId_ReturnNull() {
        ams.createResourceAccess(resourceId, accessorId);
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        JsonNode result = ams.filterResource(nonExistingUserId, nonExistingResourceId, jsonObject);

        assertThat(result).isNull();
    }
}
