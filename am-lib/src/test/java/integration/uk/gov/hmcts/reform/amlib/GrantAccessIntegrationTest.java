package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissionsForWholeDocument;

@SuppressWarnings({"LineLength"})
class GrantAccessIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
    private String resourceId;
    private String accessorId;
    private ResourceDefinition resourceDefinition;
    private String resourceType;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();
        resourceType = UUID.randomUUID().toString();
        MDC.put("caller", "Administrator");
        importerService.addResourceDefinition(
            resourceDefinition = createResourceDefinition(serviceName, resourceType, RESOURCE_NAME));
    }

    @Test
    void whenCreatingResourceAccessResourceAccessAppearsInDatabase() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, accessorId, resourceDefinition, ImmutableSet.of(READ)));
        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }

    @Test
    void whenCreatingResourceAccessMultipleEntriesAppearInDatabase() {
        Map<JsonPointer, Set<Permission>> multipleAttributePermissions = ImmutableMap.of(
            JsonPointer.valueOf(""), ImmutableSet.of(CREATE, READ, UPDATE),
            JsonPointer.valueOf("/name"), ImmutableSet.of(CREATE, READ, UPDATE));

        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, resourceDefinition, multipleAttributePermissions));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(2);
    }

    @Test
    void whenCreatingDuplicateResourceAccessEntryIsOverwritten() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, accessorId, resourceDefinition, ImmutableSet.of(READ)));
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, accessorId, resourceDefinition, ImmutableSet.of(READ)));

        assertThat(databaseHelper.countExplicitPermissions(resourceId)).isEqualTo(1);
    }

    @Test
    void whenCreatingResourceForMultipleUsersShouldAppearInDatabase() {
        service.grantExplicitResourceAccess(
            createGrantForWholeDocument(resourceId, ImmutableSet.of("User1", "User2"), resourceDefinition, ImmutableSet.of(READ)));

        assertThat(databaseHelper.findExplicitPermissions(resourceId)).hasSize(2)
            .extracting(ExplicitAccessRecord::getAccessorId).containsOnly("User1", "User2");
    }

    @Test
    void whenCreatingResourceWithInvalidRelationshipShouldThrowPersistenceException() {
        ExplicitAccessGrant nonExistingRole = createGrant(resourceId, ImmutableSet.of(accessorId),
            "NonExistingRoleName", resourceDefinition, createPermissionsForWholeDocument(ImmutableSet.of(CREATE, READ, UPDATE)));

        assertThatExceptionOfType(PersistenceException.class)
            .isThrownBy(() -> service.grantExplicitResourceAccess(nonExistingRole))
            .withMessageContaining("(relationship)=(NonExistingRoleName) is not present in table \"roles\"");
    }
}
