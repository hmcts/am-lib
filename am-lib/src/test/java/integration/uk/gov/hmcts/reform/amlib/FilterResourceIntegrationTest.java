package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.stream.Stream;

import static integration.uk.gov.hmcts.reform.amlib.TestConstants.ACCESSOR_ID;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.DATA;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static integration.uk.gov.hmcts.reform.amlib.TestConstants.createRecord;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.UPDATE;

public class FilterResourceIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void filterResource_whenRowExistWithAccessorIdAndResourceId_ReturnPassedJsonObject() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

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
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, Stream.of(CREATE, UPDATE).collect(toSet())));

        JsonNode result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        assertThat(result).isNull();
    }
}
