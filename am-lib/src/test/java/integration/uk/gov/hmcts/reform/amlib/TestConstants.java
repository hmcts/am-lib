package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import uk.gov.hmcts.reform.amlib.enums.Permissions;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;

import java.util.Set;
import java.util.stream.Stream;

import static integration.uk.gov.hmcts.reform.amlib.IntegrationBaseTest.jdbi;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.UPDATE;

public final class TestConstants {
    public static final String ACCESS_TYPE = "user";
    public static final String SERVICE_NAME = "Service 1";
    public static final String RESOURCE_TYPE = "Resource Type 1";
    public static final String RESOURCE_NAME = "resource";
    public static final String SECURITY_CLASSIFICATION = "Public";
    public static final String ACCESSOR_ID = "a";
    public static final String OTHER_ACCESSOR_ID = "b";
    public static final Set<Permissions> EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS =
        Stream.of(CREATE, READ, UPDATE).collect(toSet());
    public static final JsonNode DATA = JsonNodeFactory.instance.objectNode();

    private TestConstants() {
        //NO-OP
    }

    public static ExplicitAccessRecord createRecord(String resourceId,
                                      String accessorId,
                                      Set<Permissions> explicitPermissions) {
        return ExplicitAccessRecord.explicitAccessRecordBuilder()
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

    public static ExplicitAccessMetadata removeRecord(String resourceId) {
        return ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("")
            .build();
    }

    public static void grantAndRevokeAccessToRecord(String resourceId) {
        createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);
        removeRecord(resourceId);
    }

    public static int countResourcesById(String resourceId) {
        return jdbi.open().createQuery(
            "select count(1) from access_management where resource_id = ?")
            .bind(0, resourceId)
            .mapTo(int.class)
            .findOnly();
    }
}
