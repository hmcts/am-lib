package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_IDS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
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
        Map<JsonPointer, Set<Permission>> emptyAttributePermissions = ImmutableMap
            .<JsonPointer, Set<Permission>>builder()
            .build();

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_IDS, emptyAttributePermissions)))
            .withMessage("At least one attribute is required");
    }

    @Test
    @SuppressWarnings("PMD")
    void noPermissionsForAttributesShouldThrowException() {
        Map<JsonPointer, Set<Permission>> attributeNoPermissions = createPermissionsForWholeDocument(new HashSet<>());

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_IDS, attributeNoPermissions)))
            .withMessage("At least one permission per attribute is required");
    }

    @Test
    void whenCreatingResourceAccessResourceAccessAppearsInDatabase() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, READ_PERMISSION));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }

    @Test
    void whenCreatingResourceAccessMultipleEntriesAppearInDatabase() {
        Map<JsonPointer, Set<Permission>> multipleAttributePermissions =
            ImmutableMap.<JsonPointer, Set<Permission>>builder()
                .put(JsonPointer.valueOf(""), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS)
                .put(JsonPointer.valueOf("/name"), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS)
                .build();

        service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_IDS, multipleAttributePermissions));

        assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(4)
            .extracting(ExplicitAccessRecord::getAccessorId).contains("y", "z");
    }

    @Test
    void whenCreatingDuplicateResourceAccessEntryIsOverwritten() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, READ_PERMISSION));
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, READ_PERMISSION));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }
}
