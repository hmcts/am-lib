package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;
import uk.gov.hmcts.reform.amlib.internal.models.ExplicitAccessRecord;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_IDS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESS_MANAGEMENT_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESS_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SECURITY_CLASSIFICATION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;

class GrantAccessIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
    private String resourceId;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        importerService.addRole(ROLE_NAME, ROLE_TYPE, SECURITY_CLASSIFICATION, ACCESS_MANAGEMENT_TYPE);
        MDC.put("caller", "Administrator");
    }

    @Test
    void whenCreatingResourceAccessResourceAccessAppearsInDatabase() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, READ_PERMISSION));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }

    @Test
    void whenCreatingResourceAccessMultipleEntriesAppearInDatabase() {
        Map<JsonPointer, Set<Permission>> multipleAttributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS,
            JsonPointer.valueOf("/name"), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);

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
    void whenCreatingResourceForMultipleUsersShouldAppearInDatabase() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_IDS, READ_PERMISSION));

        assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(2)
            .extracting(ExplicitAccessRecord::getAccessorId).containsOnly("y", "z");
    }

    @Test
    void whenCreatingResourceWithInvalidRelationshipShouldThrowPersistenceException() {
        Map<JsonPointer, Set<Permission>> multipleAttributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;

        ExplicitAccessGrant nonExistingRole = ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(ACCESSOR_IDS)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(multipleAttributePermissions)
            .securityClassification(SECURITY_CLASSIFICATION)
            .relationship("NonExistingRoleName")
            .build();

        assertThatExceptionOfType(PersistenceException.class)
            .isThrownBy(() -> service.grantExplicitResourceAccess(nonExistingRole))
            .withMessageContaining("(relationship)=(NonExistingRoleName) is not present in table \"roles\"");
    }
}
