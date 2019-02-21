package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissionsForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;

class FilterResourceIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @BeforeEach
    void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    void whenRowExistWithAccessorIdAndResourceIdReturnPassedJsonObject() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, EXPLICIT_READ_PERMISSION));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .data(DATA)
            .permissions(createPermissionsForWholeDocument(EXPLICIT_READ_PERMISSION))
            .build());
    }

    @Test
    void whenRowNotExistWithAccessorIdAndResourceIdReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilterResourceResponse result = ams.filterResource(nonExistingUserId, nonExistingResourceId, DATA);

        assertThat(result).isNull();
    }

    @Test
    void whenRowExistsAndDoesntHaveReadPermissionsReturnNull() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, EXPLICIT_CREATE_PERMISSION));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, resourceId, DATA);

        assertThat(result).isNull();
    }
}
