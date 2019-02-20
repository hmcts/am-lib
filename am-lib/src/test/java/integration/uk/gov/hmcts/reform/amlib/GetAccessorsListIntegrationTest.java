package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.grantAccess;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.grantAccessForWholeDocument;

class GetAccessorsListIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @BeforeEach
    void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    void ifUserHasAccessShouldReturnUserIds() {
        Map<JsonPointer, Set<Permission>> permissions = createPermissions("", EXPLICIT_READ_PERMISSION);

        ams.grantExplicitResourceAccess(grantAccess(resourceId, ACCESSOR_ID, permissions));
        ams.grantExplicitResourceAccess(grantAccess(resourceId, OTHER_ACCESSOR_ID, permissions));

        List<String> list = ams.getAccessorsList(ACCESSOR_ID, resourceId);

        assertThat(list).containsExactly(ACCESSOR_ID, OTHER_ACCESSOR_ID);
    }

    @Test
    void ifUserHasNoAccessShouldReturnNull() {
        ams.grantExplicitResourceAccess(grantAccessForWholeDocument(resourceId, EXPLICIT_READ_PERMISSION));

        List<String> list = ams.getAccessorsList(OTHER_ACCESSOR_ID, resourceId);

        assertThat(list).isNull();
    }

    @Test
    void whenCheckingAccessToNonExistingResourceShouldReturnNull() {
        ams.grantExplicitResourceAccess(grantAccessForWholeDocument(resourceId, EXPLICIT_READ_PERMISSION));

        String nonExistingResourceId = "bbbbbbbb";

        assertThat(ams.getAccessorsList(ACCESSOR_ID, nonExistingResourceId)).isNull();
    }
}
