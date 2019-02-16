package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EMPTY_ATTRIBUTE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.MULTIPLE_ATTRIBUTE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SINGLE_ATTRIBUTE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createRecord;

public class GrantAccessIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void grantAccess_whenCreatingResourceAccess_ResourceAccessAppearsInDatabase() {
        ams.grantExplicitResourceAccess(createRecord(resourceId, ACCESSOR_ID, SINGLE_ATTRIBUTE_PERMISSION));

        assertThat(countResourcesById(resourceId)).isEqualTo(1);
    }

    @Test
    public void invalidAttributeThrowsError() {
        String invalidJsonPointer = "invalid";

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            EMPTY_ATTRIBUTE_PERMISSIONS.put(JsonPointer.valueOf(invalidJsonPointer), EXPLICIT_READ_PERMISSION))
            .withMessage("Invalid input: JSON Pointer expression must start with '/': \"" + invalidJsonPointer + "\"");
    }

    @Test
    public void grantAccess_whenCreatingResourceAccess_EmptyAttribute() {
        JsonPointer emptyJsonPointer = JsonPointer.valueOf("");
        Map<JsonPointer, Set<Permission>> emptyAttributeWithReadPermission = new ConcurrentHashMap<>();
        emptyAttributeWithReadPermission.put(emptyJsonPointer, EXPLICIT_READ_PERMISSION);

        ams.grantExplicitResourceAccess(createRecord(resourceId, ACCESSOR_ID, emptyAttributeWithReadPermission));

        assertThat(countResourcesById(resourceId)).isEqualTo(1);
    }

    @Test
    public void grantAccess_whenCreatingResourceAccess_MultipleEntries() {
        ams.grantExplicitResourceAccess(createRecord(resourceId, ACCESSOR_ID, MULTIPLE_ATTRIBUTE_PERMISSIONS));

        assertThat(countResourcesById(resourceId)).isEqualTo(2);
    }
}
