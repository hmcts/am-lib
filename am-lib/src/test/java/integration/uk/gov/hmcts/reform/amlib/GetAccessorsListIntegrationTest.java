package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;

class GetAccessorsListIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @BeforeEach
    void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    void ifUserHasAccessShouldReturnUserIds() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, EXPLICIT_READ_PERMISSION));
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, OTHER_ACCESSOR_ID, EXPLICIT_READ_PERMISSION));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).containsExactly(ACCESSOR_ID, OTHER_ACCESSOR_ID);
    }

    @Test
    void ifUserHasNoAccessShouldReturnNull() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, EXPLICIT_READ_PERMISSION));

        List<String> list = ams.getAccessorsList(OTHER_ACCESSOR_ID, resourceId);

        assertThat(list).isNull();
    }

    @Test
    void whenCheckingAccessToNonExistingResourceShouldReturnNull() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, EXPLICIT_READ_PERMISSION));

        String nonExistingResourceId = "bbbbbbbb";

        assertThat(ams.getAccessorsList(ACCESSOR_ID, nonExistingResourceId)).isNull();
    }
}
