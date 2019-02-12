package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

public class AccessManagementServiceIntegrationTest extends IntegrationBaseTest {

    private String resourceId;
    private static final String ACCESSOR_ID = "a";
    private static final String OTHER_ACCESSOR_ID = "b";
    private static final Set<Permission> EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS = Stream.of(CREATE, READ, UPDATE)
        .collect(toSet());
    private static final String ACCESS_TYPE = "user";
    private static final String SERVICE_NAME = "Service 1";
    private static final String RESOURCE_TYPE = "Resource Type 1";
    private static final String RESOURCE_NAME = "resource";
    private static final String SECURITY_CLASSIFICATION = "Public";
    private static final JsonNode DATA = JsonNodeFactory.instance.objectNode();

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void createQuery_whenCreatingResourceAccess_ResourceAccessAppearsInDatabase() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

        int count = jdbi.open().createQuery(
            "select count(1) from access_management where resource_id = ?")
            .bind(0, resourceId)
            .mapTo(int.class)
            .findOnly();

        assertThat(count).isEqualTo(1);
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

    @SuppressWarnings("PMD")
    @Test
    public void filterResource_whenRowExistWithAccessorIdAndResourceId_ReturnPassedJsonObject()
        throws UnsupportedPermissionsException {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        Map<String, Set<Permission>> attributePermissions = new HashMap<>();
        attributePermissions.put("/", EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .data(DATA)
            .permissions(attributePermissions)
            .build());
    }


    @Test
    public void filterResource_whenRowNotExistWithAccessorIdAndResourceId_ReturnNull() throws
        UnsupportedPermissionsException {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilterResourceResponse result = ams.filterResource(nonExistingUserId, nonExistingResourceId, DATA);

        assertThat(result).isNull();
    }

    @Test
    public void filterResource_whenRowExistsAndDoesntHaveReadPermissions_ReturnNull()
        throws UnsupportedPermissionsException {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, Stream.of(CREATE, UPDATE).collect(toSet())));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);
        assertThat(result).isNull();
    }

    private ExplicitAccessRecord createRecord(String resourceId,
                                              String accessorId,
                                              Set<Permission> explicitPermissions) {

        return ExplicitAccessRecord.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .explicitPermissions(explicitPermissions)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build();
    }
}
