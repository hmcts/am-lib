package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESS_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissionsForWholeDocument;

class GrantAccessIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private String resourceId;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    void noAttributesShouldThrowException() {
        Map<JsonPointer, Set<Permission>> emptyAttributePermissions = new ConcurrentHashMap<>();

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_ID, emptyAttributePermissions)))
            .withMessage("At least one attribute is required");
    }

    @Test
    @SuppressWarnings("PMD")
    void noPermissionsForAttributesShouldThrowException() {
        Map<JsonPointer, Set<Permission>> attributeNoPermissions = createPermissionsForWholeDocument(new HashSet<>());

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_ID, attributeNoPermissions)))
            .withMessage("At least one permission per attribute is required");
    }

    @Test
    void whenCreatingResourceAccessResourceAccessAppearsInDatabase() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, READ_PERMISSION));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }

    @Test
    void whenCreatingResourceAccessMultipleEntriesAppearInDatabase() {
        Map<JsonPointer, Set<Permission>> multipleAttributePermissions = new ConcurrentHashMap<>();
        multipleAttributePermissions.put(JsonPointer.valueOf(""), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);
        multipleAttributePermissions.put(JsonPointer.valueOf("/name"), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);

        service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_ID, multipleAttributePermissions));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(2);
    }

    @Test
    void whenCreatingDuplicateResourceAccessEntryIsOverwritten() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, READ_PERMISSION));
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, READ_PERMISSION));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }

    @Test
    void whenStoringStateShouldAllowEntryWithSecurityClassificationEqualNone() {
        service.grantExplicitResourceAccess(ExplicitAccessGrant.builder()
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .resourceId(resourceId)
            .resourceName(RESOURCE_NAME)
            .resourceType(RESOURCE_TYPE)
            .serviceName(SERVICE_NAME)
            .attributePermissions(createPermissions("/__state/closed", READ_PERMISSION))
            .securityClassification(SecurityClassification.NONE)
            .build());

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }

    @Test
    void whenStoringEventShouldAllowEntryWithSecurityClassificationEqualNone() {
        service.grantExplicitResourceAccess(ExplicitAccessGrant.builder()
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .resourceId(resourceId)
            .resourceName(RESOURCE_NAME)
            .resourceType(RESOURCE_TYPE)
            .serviceName(SERVICE_NAME)
            .attributePermissions(createPermissions("/__event/enterPlea", READ_PERMISSION))
            .securityClassification(SecurityClassification.NONE)
            .build());

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }
}
