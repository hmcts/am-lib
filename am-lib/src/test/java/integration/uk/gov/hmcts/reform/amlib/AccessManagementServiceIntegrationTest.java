package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class AccessManagementServiceIntegrationTest extends IntegrationBaseTest {

    private String resourceId;
    private JsonNode jsonObject = JsonNodeFactory.instance.objectNode();

    @Before
    public void testSetup() {
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
    public void filterResource_whenRowExistWithAccessorIdAndResourceId_ReturnPassedJsonObject() {
        String userId = UUID.randomUUID().toString();
        ams.createResourceAccess(resourceId, userId);

        JsonNode result = ams.filterResource(userId, resourceId, jsonObject);

        assertThat(result).isEqualTo(jsonObject);
    }

    @Test
    public void filterResource_whenRowNotExistWithAccessorIdAndResourceId_ReturnNull() {
        String userId = "def";
        ams.createResourceAccess(resourceId, userId);
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        JsonNode result = ams.filterResource(nonExistingUserId, nonExistingResourceId, jsonObject);

        assertThat(result).isNull();
    }
}
