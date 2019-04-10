package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

@SuppressWarnings({"LineLength", "PMD.TooManyMethods"})
class GetResourceDefinitionsWithCreatePermissionIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);

    private final ResourceDefinition resource = buildResource(RESOURCE_NAME);
    private final ResourceDefinition otherResource = buildResource(RESOURCE_NAME + "2");

    @BeforeEach
    void setUp() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.addRole(OTHER_ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.addResourceDefinition(otherResource.getServiceName(), otherResource.getResourceType(), otherResource.getResourceName());
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
        Set<Permission> permissions = ImmutableSet.of(Permission.READ, Permission.CREATE);

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

        assertThat(result).containsExactly(otherResource, resource);
    }

    @Test
    void shouldRetrieveMultipleResourceDefinitionsWhenDefaultPermissionsExistForDifferentRoles() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            "", CREATE_PERMISSION, resource, ROLE_NAME));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            "", CREATE_PERMISSION, otherResource, OTHER_ROLE_NAME));

        Set<String> userRoles = ImmutableSet.of(ROLE_NAME, OTHER_ROLE_NAME);

        Set<ResourceDefinition> result = service.getResourceDefinitionsWithRootCreatePermission(userRoles);

        assertThat(result).containsExactly(otherResource, resource);
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

    @Test
    void whenMultipleRoleBasedAccessRecordsShouldOnlyReturnDefinitionsAllowedByRoleSecurityClassification() {
        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .serviceName(resource.getServiceName())
            .resourceType(resource.getResourceType())
            .resourceName(resource.getResourceName())
            .attributePermissions(createPermissionsForAttribute(ROOT_ATTRIBUTE, CREATE_PERMISSION, SecurityClassification.PUBLIC))
            .build());

        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .serviceName(otherResource.getServiceName())
            .resourceType(otherResource.getResourceType())
            .resourceName(otherResource.getResourceName())
            .attributePermissions(createPermissionsForAttribute(ROOT_ATTRIBUTE, CREATE_PERMISSION, SecurityClassification.PRIVATE))
            .build());

        Set<ResourceDefinition> result = service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(ROLE_NAME));

        assertThat(result).containsExactly(resource);
    }

    @Test
    void whenTwoRolesWithDifferentSecurityClassificationShouldUseTheHighestSecurityClassificationToFilter() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.RESTRICTED, AccessType.ROLE_BASED);

        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .serviceName(resource.getServiceName())
            .resourceType(resource.getResourceType())
            .resourceName(resource.getResourceName())
            .attributePermissions(createPermissionsForAttribute(ROOT_ATTRIBUTE, CREATE_PERMISSION, SecurityClassification.PRIVATE))
            .build());

        Set<ResourceDefinition> result = service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(ROLE_NAME));

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
