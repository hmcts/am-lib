package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SINGLE_ATTRIBUTE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createRecord;

public class FilterResourceIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void filterResource_whenRowExistWithAccessorIdAndResourceId_ReturnPassedJsonObject() {
        ams.grantExplicitResourceAccess(createRecord(resourceId, ACCESSOR_ID, SINGLE_ATTRIBUTE_PERMISSION));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .data(DATA)
            .permissions(SINGLE_ATTRIBUTE_PERMISSION)
            .build());
    }

    @Test
    public void filterResource_whenRowNotExistWithAccessorIdAndResourceId_ReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilterResourceResponse result = ams.filterResource(nonExistingUserId, nonExistingResourceId, DATA);

        assertThat(result).isNull();
    }

    @Test
    public void filterResource_whenRowExistsAndDoesntHaveReadPermissions_ReturnNull() {
        Map<JsonPointer, Set<Permission>> rootLevelCreatePermission = new ConcurrentHashMap<>();
        rootLevelCreatePermission.put(JsonPointer.valueOf("/"), EXPLICIT_CREATE_PERMISSION);

        ams.grantExplicitResourceAccess(createRecord(resourceId, ACCESSOR_ID, rootLevelCreatePermission));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        assertThat(result).isNull();
    }
}
