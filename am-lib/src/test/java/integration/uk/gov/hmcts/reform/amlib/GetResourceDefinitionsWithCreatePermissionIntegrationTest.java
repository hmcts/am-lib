package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class GetResourceDefinitionsWithCreatePermissionIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);

    private final ResourceDefinition resource = buildResource(RESOURCE_NAME);
    private final ResourceDefinition otherResource = buildResource(RESOURCE_NAME + "2");

    @BeforeEach
    void setUp() {
        importerService.addResourceDefinition(
            otherResource.getServiceName(),
            otherResource.getResourceType(),
            otherResource.getResourceName());
    }

    @Test
    void shouldRetrieveResourceDefinitionWhenRecordExists() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, CREATE_PERMISSION));

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(ROLE_NAME));

        assertThat(result).containsExactly(ResourceDefinition.builder()
            .resourceName(RESOURCE_NAME)
            .resourceType(RESOURCE_TYPE)
            .serviceName(SERVICE_NAME)
            .build());
    }

    @Test
    void shouldRetrieveResourceDefinitionWhenRecordExistsWithMultiplePermissions() {
        Set<Permission> permissions = ImmutableSet.of(READ, CREATE);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, permissions));

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(ROLE_NAME));

        assertThat(result).containsExactly(ResourceDefinition.builder()
            .resourceName(RESOURCE_NAME)
            .resourceType(RESOURCE_TYPE)
            .serviceName(SERVICE_NAME)
            .build());
    }

    @Test
    void shouldNotRetrieveResourceDefinitionWhenRecordExistsWithoutRootAttribute() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            "/adult", CREATE_PERMISSION, resource, ROLE_NAME));

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(ROLE_NAME));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenRecordExistsButNoCreatePermission() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            "", READ_PERMISSION, resource, ROLE_NAME));

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(ROLE_NAME));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoRecords() {
        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(Collections.singleton(ROLE_NAME));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRetrieveMultipleResourceDefinitionsWhenMultipleRecordExistsForTheSameRole() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            "", CREATE_PERMISSION, resource, ROLE_NAME));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            "", CREATE_PERMISSION, otherResource, ROLE_NAME));

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(ROLE_NAME));

        assertThat(result).containsExactly(resource, otherResource);
    }

    @Test
    void shouldRetrieveMultipleResourceDefinitionsWhenDefaultPermissionsExistForDifferentRoles() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            "", CREATE_PERMISSION, resource, ROLE_NAME));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            "", CREATE_PERMISSION, otherResource, OTHER_ROLE_NAME));

        Set<String> userRoles = ImmutableSet.of(ROLE_NAME, OTHER_ROLE_NAME);

        Set<ResourceDefinition> result = service.getResourceDefinitionsWithRootCreatePermission(userRoles);

        assertThat(result).containsExactly(resource, otherResource);
    }

    @Test
    void shouldRetrieveOnlyOneResourceDefinitionWhenUserHasAccessWithTwoRoles() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            "", CREATE_PERMISSION, resource, ROLE_NAME));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            "", CREATE_PERMISSION, resource, OTHER_ROLE_NAME));

        Set<String> userRoles = ImmutableSet.of(ROLE_NAME, OTHER_ROLE_NAME);

        Set<ResourceDefinition> result = service.getResourceDefinitionsWithRootCreatePermission(userRoles);

        assertThat(result).containsExactly(resource);
    }

    private ResourceDefinition buildResource(String resourceName) {
        return ResourceDefinition.builder()
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(resourceName)
            .build();
    }
}
