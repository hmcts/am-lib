package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permissions;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.UPDATE;

public class AccessManagementServiceIntegrationTest extends IntegrationBaseTest {

    private static final String ACCESSOR_ID = "a";
    private static final String OTHER_ACCESSOR_ID = "b";
    private static final Set<Permissions> EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS = Stream.of(CREATE, READ, UPDATE)
        .collect(toSet());
    private static final String ACCESS_TYPE = "user";
    private static final String SERVICE_NAME = "Service 1";
    private static final String RESOURCE_TYPE = "Resource Type 1";
    private static final String RESOURCE_NAME = "resource";
    private static final String SECURITY_CLASSIFICATION = "Public";
    private static final String ATTRIBUTE = "/test";
    private static final JsonNode DATA = JsonNodeFactory.instance.objectNode();

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
        jdbi.open().execute("delete from access_management");
    }

    @Test
    public void createQuery_whenCreatingResourceAccess_ResourceAccessAppearsInDatabase() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS, ATTRIBUTE));

        int count = jdbi.open().createQuery(
            "select count(1) from access_management where resource_id = ?")
            .bind(0, resourceId)
            .mapTo(int.class)
            .findOnly();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void createQuery_whenCreatingResourceAccess_InvalidAttributeThrowsError() {
        String invalidJsonPointer = "invalid";

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            ams.createResourceAccess(
                createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS, invalidJsonPointer)))
            .withMessage("Invalid input: JSON Pointer expression must start with '/': \"" + invalidJsonPointer + "\"");
    }

    @Test
    public void createQuery_whenCreatingResourceAccess_EmptyAttribute() {
        String emptyJsonPointer = "";

        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS, emptyJsonPointer));

        int count = jdbi.open().createQuery(
            "select count(1) from access_management where resource_id = ?")
            .bind(0, resourceId)
            .mapTo(int.class)
            .findOnly();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void whenCheckingAccess_ifUserHasAccess_ShouldReturnUserIds() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS, ATTRIBUTE));
        ams.createResourceAccess(createRecord(resourceId, OTHER_ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS, ATTRIBUTE));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).containsExactly(ACCESSOR_ID, OTHER_ACCESSOR_ID);
    }

    @Test
    public void whenCheckingAccess_ifUserHasNoAccess_ShouldReturnNull() {
        ams.createResourceAccess(createRecord(resourceId, OTHER_ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS, ATTRIBUTE));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).isNull();
    }

    @Test
    public void whenCheckingAccess_ToNonExistingResource_ShouldReturnNull() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS, ATTRIBUTE));

        String nonExistingResourceId = "bbbbbbbb";

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, nonExistingResourceId);

        assertThat(list).isNull();
    }

    @Test
    public void filterResource_whenRowExistWithAccessorIdAndResourceId_ReturnPassedJsonObject() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS, ATTRIBUTE));

        JsonNode result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        assertThat(result).isEqualTo(DATA);
    }

    @Test
    public void filterResource_whenRowNotExistWithAccessorIdAndResourceId_ReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        JsonNode result = ams.filterResource(nonExistingUserId, nonExistingResourceId, DATA);

        assertThat(result).isNull();
    }

    @Test
    public void filterResource_whenRowExistsAndDoesntHaveReadPermissions_ReturnNull() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, Stream.of(CREATE, UPDATE).collect(toSet()), ATTRIBUTE));

        JsonNode result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        assertThat(result).isNull();
    }

    private ExplicitAccessRecord createRecord(String resourceId,
                                              String accessorId,
                                              Set<Permissions> explicitPermissions,
                                              String attribute) {
        return ExplicitAccessRecord.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .explicitPermissions(explicitPermissions)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute(attribute)
            .securityClassification(SECURITY_CLASSIFICATION)
            .build();
    }
}
