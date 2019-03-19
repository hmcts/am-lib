package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.Pair;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.UPDATE_PERMISSION;

class GetRolePermissionsIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);

    @BeforeEach
    void setUp() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.addRole(
            OTHER_ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);

        Map.Entry<Set<Permission>, SecurityClassification> readPermission =
            new Pair<>(READ_PERMISSION, SecurityClassification.PUBLIC);

        Map.Entry<Set<Permission>, SecurityClassification> createPermission =
            new Pair<>(CREATE_PERMISSION, SecurityClassification.PUBLIC);

        Map.Entry<Set<Permission>, SecurityClassification> updatePermission =
            new Pair<>(ImmutableSet.of(Permission.UPDATE), SecurityClassification.PUBLIC);

        Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>> attributePermissionsForRole =
            ImmutableMap.<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>>builder()
                .put(JsonPointer.valueOf("/child"), readPermission)
                .put(JsonPointer.valueOf("/parent/age"), createPermission)
                .put(JsonPointer.valueOf("/address/street/line1"), createPermission)
                .build();

        importerService.grantDefaultPermission(
            DefaultPermissionGrant.builder()
                .roleName(ROLE_NAME)
                .serviceName(SERVICE_NAME)
                .resourceType(RESOURCE_TYPE)
                .resourceName(RESOURCE_NAME)
                .attributePermissions(attributePermissionsForRole)
                .build());

        Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>> attributePermissionsForOtherRole =
            ImmutableMap.<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>>builder()
                .put(JsonPointer.valueOf(""), updatePermission)
                .put(JsonPointer.valueOf("/address"), createPermission)
                .build();

        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(OTHER_ROLE_NAME)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(attributePermissionsForOtherRole)
            .build());
    }

    @Test
    void returnListOfPermissionsForRoleName() {
        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(buildResource(SERVICE_NAME,
            RESOURCE_TYPE, RESOURCE_NAME), ROLE_NAMES);

        assertThat(accessRecord)
            .hasSize(3)
            .containsEntry(JsonPointer.valueOf("/child"), READ_PERMISSION)
            .containsEntry(JsonPointer.valueOf("/parent/age"), CREATE_PERMISSION)
            .containsEntry(JsonPointer.valueOf("/address/street/line1"), CREATE_PERMISSION);
    }

    @Test
    void shouldReturnNullWhenServiceNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(
            buildResource("Unknown Service", RESOURCE_TYPE, RESOURCE_NAME), ROLE_NAMES);

        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenResourceTypeDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(
            buildResource(SERVICE_NAME, "Unknown Resource Type", RESOURCE_NAME), ROLE_NAMES);

        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenResourceNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(
            buildResource(SERVICE_NAME, RESOURCE_TYPE, "Unknown Resource Name"), ROLE_NAMES);

        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenDefaultRoleNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(
            buildResource(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME), ImmutableSet.of("Unknown Role"));

        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldMergeDataAsExpectedWhenRetrievingPermissionsForMultipleRoles() {
        Set<String> userRoles = ImmutableSet.of(ROLE_NAME, OTHER_ROLE_NAME);
        Set<Permission> readUpdate = Stream.of(Permission.READ, Permission.UPDATE).collect(toSet());
        Set<Permission> createUpdate = Stream.of(Permission.CREATE, Permission.UPDATE).collect(toSet());

        Map<JsonPointer, Set<Permission>> accessRecord = service.getRolePermissions(
            buildResource(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME), userRoles);

        assertThat(accessRecord)
            .hasSize(5)
            .containsEntry(JsonPointer.valueOf(""), UPDATE_PERMISSION)
            .containsEntry(JsonPointer.valueOf("/address"), createUpdate)
            .containsEntry(JsonPointer.valueOf("/address/street/line1"), createUpdate)
            .containsEntry(JsonPointer.valueOf("/child"), readUpdate)
            .containsEntry(JsonPointer.valueOf("/parent/age"), createUpdate);
    }

    private ResourceDefinition buildResource(String serviceName, String resourceType, String resourceName) {
        return ResourceDefinition.builder()
            .resourceName(resourceName)
            .resourceType(resourceType)
            .serviceName(serviceName)
            .build();
    }
}
