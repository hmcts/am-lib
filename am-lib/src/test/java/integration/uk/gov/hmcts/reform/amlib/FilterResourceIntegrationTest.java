package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createRecord;

class FilterResourceIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @BeforeEach
    void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    void filterResource_whenRowExistWithAccessorIdAndResourceId_ReturnPassedJsonObject() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        Map<String, Set<Permission>> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put("/", EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
                .resourceId(resourceId)
                .data(DATA)
                .permissions(attributePermissions)
                .build());
    }

    @Test
    void filterResource_whenRowNotExistWithAccessorIdAndResourceId_ReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilterResourceResponse result = ams.filterResource(nonExistingUserId, nonExistingResourceId, DATA);

        assertThat(result).isNull();
    }

    @Test
    void filterResource_whenRowExistsAndDoesntHaveReadPermissions_ReturnNull() {
        ams.createResourceAccess(createRecord(resourceId, ACCESSOR_ID, Stream.of(CREATE, UPDATE).collect(toSet())));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        assertThat(result).isNull();
    }
}
