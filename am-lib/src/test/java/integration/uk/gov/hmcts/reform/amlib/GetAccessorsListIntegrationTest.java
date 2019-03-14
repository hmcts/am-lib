package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_IDS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DIFFERENT_ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DIFFERENT_ACCESSOR_IDS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;

class GetAccessorsListIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private String resourceId;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    @SuppressWarnings("LineLength")
    void ifUserHasAccessShouldReturnUserIds() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId,
            ACCESSOR_IDS, READ_PERMISSION));
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId,
            DIFFERENT_ACCESSOR_IDS, READ_PERMISSION));

        List<String> list = service.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).containsExactly("a","d","e","f");
    }

    @Test
    void ifUserHasNoAccessShouldReturnNull() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_IDS, READ_PERMISSION));

        List<String> list = service.getAccessorsList(DIFFERENT_ACCESSOR_ID, resourceId);

        assertThat(list).isNull();
    }

    @Test
    void whenCheckingAccessToNonExistingResourceShouldReturnNull() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_IDS, READ_PERMISSION));

        String nonExistingResourceId = "bbbbbbbb";

        assertThat(service.getAccessorsList(ACCESSOR_ID, nonExistingResourceId)).isNull();
    }
}
