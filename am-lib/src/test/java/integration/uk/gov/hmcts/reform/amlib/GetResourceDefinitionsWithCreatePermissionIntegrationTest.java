package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PRIVATE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.RESTRICTED;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;

@SuppressWarnings({"PMD.TooManyMethods", "LineLength"})
class GetResourceDefinitionsWithCreatePermissionIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
    private ResourceDefinition resourceDefinition;
    private ResourceDefinition otherResource;
    private String roleName;
    private String otherRoleName;

    @BeforeEach
    void setUp() {
        roleName = UUID.randomUUID().toString();
        otherRoleName = UUID.randomUUID().toString();
        importerService.addRole(roleName, IDAM, PUBLIC, EXPLICIT);
        importerService.addRole(otherRoleName, IDAM, PUBLIC, EXPLICIT);
        importerService.addResourceDefinition(resourceDefinition =
            createResourceDefinition(serviceName, UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        importerService.addResourceDefinition(otherResource =
            createResourceDefinition(serviceName, UUID.randomUUID().toString(), UUID.randomUUID().toString()));
    }

    @Test
    void shouldRetrieveResourceDefinitionWhenRecordExists() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            roleName, resourceDefinition, ImmutableSet.of(CREATE), ""));

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(roleName));

        assertThat(result).containsExactly(resourceDefinition);
    }

    @Test
    void shouldRetrieveResourceDefinitionWhenRecordExistsWithMultiplePermissions() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            roleName, resourceDefinition, ImmutableSet.of(READ, CREATE), ""));


        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(roleName));

        assertThat(result).containsExactly(resourceDefinition);
    }

    @Test
    void shouldNotRetrieveResourceDefinitionWhenRecordExistsWithoutRootAttribute() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(
            roleName, resourceDefinition, ImmutableSet.of(CREATE), "/adult"));

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(roleName));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenRecordExistsButNoCreatePermission() {
        grantRootPermission(roleName, resourceDefinition, READ, PUBLIC);

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(roleName));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoRecords() {
        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(Collections.singleton(roleName));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRetrieveMultipleResourceDefinitionsWhenMultipleRecordExistsForTheSameRole() {
        grantRootPermission(roleName, resourceDefinition, CREATE, PUBLIC);
        grantRootPermission(roleName, otherResource, CREATE, PUBLIC);

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(roleName));

        assertThat(result).containsExactlyInAnyOrder(resourceDefinition, otherResource);
    }

    @Test
    void shouldRetrieveMultipleResourceDefinitionsWhenDefaultPermissionsExistForDifferentRoles() {
        grantRootPermission(roleName, resourceDefinition, CREATE, PUBLIC);
        grantRootPermission(otherRoleName, otherResource, CREATE, PUBLIC);

        Set<String> userRoles = ImmutableSet.of(roleName, otherRoleName);

        Set<ResourceDefinition> result = service.getResourceDefinitionsWithRootCreatePermission(userRoles);

        assertThat(result).containsExactlyInAnyOrder(resourceDefinition, otherResource);
    }

    @Test
    void shouldRetrieveOnlyOneResourceDefinitionWhenUserHasAccessWithTwoRoles() {
        grantRootPermission(roleName, resourceDefinition, CREATE, PUBLIC);
        grantRootPermission(otherRoleName, resourceDefinition, CREATE, PUBLIC);

        Set<String> userRoles = ImmutableSet.of(roleName, otherRoleName);

        Set<ResourceDefinition> result = service.getResourceDefinitionsWithRootCreatePermission(userRoles);

        assertThat(result).containsExactly(resourceDefinition);
    }

    @Test
    void whenMultipleRoleBasedAccessRecordsShouldOnlyReturnDefinitionsAllowedByRoleSecurityClassification() {
        String rolePublic = "RoleWithOnlyPublic";

        importerService.addRole(rolePublic, IDAM, PUBLIC, ROLE_BASED);

        grantRootPermission(rolePublic, resourceDefinition, CREATE, PUBLIC);
        grantRootPermission(rolePublic, otherResource, CREATE, PRIVATE);

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(rolePublic));

        assertThat(result).containsExactly(resourceDefinition);
    }

    @Test
    void whenRoleDoesNotHaveHighEnoughSecurityClassificationShouldReturnEmptyList() {
        String rolePublic = "RoleWithOnlyPublic";

        importerService.addRole(rolePublic, IDAM, PUBLIC, ROLE_BASED);

        grantRootPermission(rolePublic, resourceDefinition, CREATE, RESTRICTED);

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(rolePublic));

        assertThat(result).isEmpty();
    }

    @Test
    void whenTwoRolesWithDifferentSecurityClassificationShouldUseTheHighestSecurityClassificationToFilter() {
        String rolePublic = "RoleWithOnlyPublic";
        String rolePrivate = "RoleWithPrivate";

        importerService.addRole(rolePublic, IDAM, PUBLIC, ROLE_BASED);
        importerService.addRole(rolePrivate, IDAM, PRIVATE, ROLE_BASED);

        grantRootPermission(rolePublic, resourceDefinition, CREATE, PRIVATE);

        Set<ResourceDefinition> result =
            service.getResourceDefinitionsWithRootCreatePermission(ImmutableSet.of(rolePublic, rolePrivate));

        assertThat(result).containsExactly(resourceDefinition);
    }

    @SuppressWarnings("LineLength")
    private void grantRootPermission(String roleName, ResourceDefinition resourceDefinition, Permission permission, SecurityClassification securityClassification) {
        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(resourceDefinition.getServiceName())
                .resourceType(resourceDefinition.getResourceType())
                .resourceName(resourceDefinition.getResourceName())
                .build())
            .attributePermissions(createPermissionsForAttribute(ROOT_ATTRIBUTE, ImmutableSet.of(permission), securityClassification))
            .build());
    }
}
