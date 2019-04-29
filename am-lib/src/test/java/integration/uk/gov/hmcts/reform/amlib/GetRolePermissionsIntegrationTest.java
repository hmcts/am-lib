package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.Pair;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;

class GetRolePermissionsIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
    private ResourceDefinition resourceDefinition;
    private String resourceType;


    @BeforeEach
    void setUp() {
        resourceType = UUID.randomUUID().toString();
        importerService.addResourceDefinition(
            resourceDefinition = createResourceDefinition(serviceName, resourceType, RESOURCE_NAME));

        Map.Entry<Set<Permission>, SecurityClassification> readPermission = new Pair<>(ImmutableSet.of(READ), PUBLIC);
        Map.Entry<Set<Permission>, SecurityClassification> createPermission = new Pair<>(
            ImmutableSet.of(CREATE), PUBLIC);

        Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>> attributePermissionsForRole =
            ImmutableMap.of(
                JsonPointer.valueOf("/child"), readPermission,
                JsonPointer.valueOf("/parent/age"), createPermission,
                JsonPointer.valueOf("/address/street/line1"), createPermission
            );

        importerService.grantDefaultPermission(
            DefaultPermissionGrant.builder()
                .roleName(ROLE_NAME)
                .resourceDefinition(resourceDefinition)
                .attributePermissions(attributePermissionsForRole)
                .build());

        Map.Entry<Set<Permission>, SecurityClassification> updatePermission = new Pair<>(
            ImmutableSet.of(UPDATE), PUBLIC);

        Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>> attributePermissionsForOtherRole =
            ImmutableMap.of(
                JsonPointer.valueOf(""), updatePermission,
                JsonPointer.valueOf("/address"), createPermission
            );

        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(OTHER_ROLE_NAME)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(attributePermissionsForOtherRole)
            .build());
    }

    @Test
    void returnListOfPermissionsForRoleName() {
        Map<JsonPointer, Set<Permission>> accessRecord =
            service.getRolePermissions(resourceDefinition, ROLE_NAMES);

        assertThat(accessRecord)
            .hasSize(3)
            .containsEntry(JsonPointer.valueOf("/child"), ImmutableSet.of(READ))
            .containsEntry(JsonPointer.valueOf("/parent/age"), ImmutableSet.of(CREATE))
            .containsEntry(JsonPointer.valueOf("/address/street/line1"), ImmutableSet.of(CREATE));
    }

    @Test
    void shouldReturnNullWhenServiceNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(
            buildResource("Unknown Service", resourceType, RESOURCE_NAME), ROLE_NAMES);

        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenResourceTypeDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(
            buildResource(serviceName, "Unknown Resource Type", RESOURCE_NAME), ROLE_NAMES);

        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenResourceNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(
            buildResource(serviceName, resourceType, "Unknown Resource Name"), ROLE_NAMES);

        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenDefaultRoleNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(
            resourceDefinition, ImmutableSet.of("Unknown Role"));

        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldMergeDataAsExpectedWhenRetrievingPermissionsForMultipleRoles() {
        Set<String> userRoles = ImmutableSet.of(ROLE_NAME, OTHER_ROLE_NAME);

        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(
            resourceDefinition, userRoles);

        assertThat(accessRecord)
            .hasSize(5)
            .containsEntry(JsonPointer.valueOf(""), ImmutableSet.of(UPDATE))
            .containsEntry(JsonPointer.valueOf("/address"), ImmutableSet.of(CREATE))
            .containsEntry(JsonPointer.valueOf("/address/street/line1"), ImmutableSet.of(CREATE))
            .containsEntry(JsonPointer.valueOf("/child"), ImmutableSet.of(READ, UPDATE))
            .containsEntry(JsonPointer.valueOf("/parent/age"), ImmutableSet.of(CREATE, UPDATE));
    }

    private ResourceDefinition buildResource(String serviceName, String resourceType, String resourceName) {
        return ResourceDefinition.builder()
            .resourceName(resourceName)
            .resourceType(resourceType)
            .serviceName(serviceName)
            .build();
    }
}
