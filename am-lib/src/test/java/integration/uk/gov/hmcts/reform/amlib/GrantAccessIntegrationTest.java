package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.TransactionRolledbackException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.grantAccess;

class GrantAccessIntegrationTest extends IntegrationBaseTest {

    private String resourceId;

    @BeforeEach
    void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    void emptyPermissionsMap_shouldThrowException() {
        Map<JsonPointer, Set<Permission>> emptyAttributePermissions = new ConcurrentHashMap<>();

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
            ams.grantExplicitResourceAccess(grantAccess(resourceId, ACCESSOR_ID, emptyAttributePermissions)))
            .withMessage("Attribute permissions cannot be empty");
    }

    @Test
    void whenCreatingResourceAccess_ResourceAccessAppearsInDatabase() throws TransactionRolledbackException {
        Map<JsonPointer, Set<Permission>> singleAttributePermission = new ConcurrentHashMap<>();
        singleAttributePermission.put(JsonPointer.valueOf(""), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);

        ams.grantExplicitResourceAccess(grantAccess(resourceId, ACCESSOR_ID, singleAttributePermission));

        assertThat(countResourcesById(resourceId)).isEqualTo(1);
    }

    @Test
    void whenCreatingResourceAccess_MultipleEntriesAppearInDatabase() throws TransactionRolledbackException {
        Map<JsonPointer, Set<Permission>> multipleAttributePermissions = new ConcurrentHashMap<>();
        multipleAttributePermissions.put(JsonPointer.valueOf(""), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);
        multipleAttributePermissions.put(JsonPointer.valueOf("/name"), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);

        ams.grantExplicitResourceAccess(grantAccess(resourceId, ACCESSOR_ID, multipleAttributePermissions));

        assertThat(countResourcesById(resourceId)).isEqualTo(2);
    }

    @Test
    void whenCreatingDuplicateResourceAccess_EntryIsOverwritten() throws TransactionRolledbackException {
        Map<JsonPointer, Set<Permission>> duplicateAttributePermissions = new ConcurrentHashMap<>();
        duplicateAttributePermissions.put(JsonPointer.valueOf("/name"), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);
        duplicateAttributePermissions.put(JsonPointer.valueOf("/name"), EXPLICIT_CREATE_PERMISSION);

        ams.grantExplicitResourceAccess(grantAccess(resourceId, ACCESSOR_ID, duplicateAttributePermissions));

        assertThat(countResourcesById(resourceId)).isEqualTo(1);
    }
}
