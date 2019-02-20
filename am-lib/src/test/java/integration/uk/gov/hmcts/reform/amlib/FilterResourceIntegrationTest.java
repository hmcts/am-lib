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
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.grantAccess;

public class FilterResourceIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void whenRowExistWithAccessorIdAndResourceId_ReturnPassedJsonObject() {
        Map<JsonPointer, Set<Permission>> singleAttributePermission = new ConcurrentHashMap<>();
        singleAttributePermission.put(JsonPointer.valueOf("/"), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);

        ams.grantExplicitResourceAccess(grantAccess(resourceId, ACCESSOR_ID, singleAttributePermission));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .data(DATA)
            .permissions(singleAttributePermission)
            .build());
    }

    @Test
    public void whenRowNotExistWithAccessorIdAndResourceId_ReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilterResourceResponse result = ams.filterResource(nonExistingUserId, nonExistingResourceId, DATA);

        assertThat(result).isNull();
    }

    @Test
    public void whenRowExistsAndDoesntHaveReadPermissions_ReturnNull() {
        Map<JsonPointer, Set<Permission>> rootLevelCreatePermission = new ConcurrentHashMap<>();
        rootLevelCreatePermission.put(JsonPointer.valueOf("/"), EXPLICIT_CREATE_PERMISSION);

        ams.grantExplicitResourceAccess(grantAccess(resourceId, ACCESSOR_ID, rootLevelCreatePermission));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        assertThat(result).isNull();
    }
}
