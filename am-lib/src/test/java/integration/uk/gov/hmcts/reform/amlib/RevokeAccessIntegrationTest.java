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
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createMetadata;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.grantAccess;

public class RevokeAccessIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @Before
    public void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    public void revokeResourceAccess_whenRevokingResourceAccess_ResourceAccessRemovedFromDatabase() {
        Map<JsonPointer, Set<Permission>> singleAttributePermission = new ConcurrentHashMap<>();
        singleAttributePermission.put(JsonPointer.valueOf(""), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);


        ams.grantExplicitResourceAccess(grantAccess(resourceId, ACCESSOR_ID, singleAttributePermission));
        ams.revokeResourceAccess(createMetadata(resourceId));

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }

    @Test
    public void revokeResourceAccess_whenRevokingResourceAccessThatDoesNotExist_NoErrorExpected() {
        ams.revokeResourceAccess(createMetadata("4"));

        assertThat(countResourcesById(resourceId)).isEqualTo(0);
    }
}
