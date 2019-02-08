package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permissions;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.ExplicitPermissions;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class AccessManagementServiceIntegrationTest extends IntegrationBaseTest {

    private String resourceId;
    private static final String ACCESSOR_ID = "a";
    private static final String OTHER_ACCESSOR_ID = "b";
    private static final String ACCESS_TYPE = "user";
    private static final String SERVICE_NAME = "Service 1";
    private static final String RESOURCE_TYPE = "Resource Type 1";
    private static final String RESOURCE_NAME = "resource";
    private static final String SECURITY_CLASSIFICATION = "Public";


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
        ams.createResourceAccess(ExplicitAccessRecord.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .explicitPermissions(explicitReadCreateUpdatePermissions)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());

        int count = jdbi.open().createQuery(
            "select count(1) from access_management where resource_id = ?")
            .bind(0, resourceId)
            .mapTo(int.class)
            .findOnly();

        assertThat(count).isEqualTo(1);
    }


    @Test
    public void whenCheckingAccess_ifUserHasAccess_ShouldReturnUserIds() {
        ams.createResourceAccess(ExplicitAccessRecord.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .explicitPermissions(explicitReadCreateUpdatePermissions)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());

        ams.createResourceAccess(ExplicitAccessRecord.builder()
            .resourceId(resourceId)
            .accessorId(OTHER_ACCESSOR_ID)
            .explicitPermissions(explicitReadCreateUpdatePermissions)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());


        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).containsExactly(ACCESSOR_ID, OTHER_ACCESSOR_ID);
    }

    @Test
    public void whenCheckingAccess_ifUserHasNoAccess_ShouldReturnNull() {
        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).isNull();
    }

    @Test
    public void whenCheckingAccess_ToNonExistingResource_ShouldReturnNull() {
        String nonExistingResourceId = "bbbbbbbb";

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, nonExistingResourceId);

        assertThat(list).isNull();
    }

    @Test
    public void filterResource_whenRowExistWithAccessorIdAndResourceId_ReturnPassedJsonObject() {
        ams.createResourceAccess(ExplicitAccessRecord.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .explicitPermissions(explicitReadCreateUpdatePermissions)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());

        JsonNode result = ams.filterResource(ACCESSOR_ID, resourceId, jsonObject);

        assertThat(result).isEqualTo(jsonObject);
    }


    @Test
    public void filterResource_whenRowNotExistWithAccessorIdAndResourceId_ReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        JsonNode result = ams.filterResource(nonExistingUserId, nonExistingResourceId, jsonObject);

        assertThat(result).isNull();
    }

    @Test
    public void filterResource_whenRowExistsAndDoesntHaveReadPermissions_ReturnNull() {
        ams.createResourceAccess(ExplicitAccessRecord.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .explicitPermissions(new ExplicitPermissions(Permissions.UPDATE))
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build());


        JsonNode result = ams.filterResource(ACCESSOR_ID, resourceId, jsonObject);

        assertThat(result).isNull();
    }
}
