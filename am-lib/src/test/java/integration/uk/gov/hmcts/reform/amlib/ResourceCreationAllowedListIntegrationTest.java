package integration.uk.gov.hmcts.reform.amlib;

import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class ResourceCreationAllowedListIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);

    @Test
    void shouldRetrieveResourceDefinitionWhenRecordExists() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, CREATE_PERMISSION));

        List<ResourceDefinition> result = service.resourceCreationAllowedList(Collections.singleton(ROLE_NAME));

        assertThat(result).containsExactly(ResourceDefinition.builder()
            .resourceName(RESOURCE_NAME)
            .resourceType(RESOURCE_TYPE)
            .serviceName(SERVICE_NAME)
            .build());
    }

    @Test
    void shouldReturnNullWhenRecordExistsButNoCreatePermission() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, READ_PERMISSION));

        List<ResourceDefinition> result = service.resourceCreationAllowedList(Collections.singleton(ROLE_NAME));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnNullWhenNoRecord() {
        List<ResourceDefinition> result = service.resourceCreationAllowedList(Collections.singleton(ROLE_NAME));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRetrieveMultipleResourceDefinitionsWhenMultipleRecordExists() {
        ResourceDefinition resource = resourceBuilder(RESOURCE_NAME);
        ResourceDefinition resource2 = resourceBuilder(RESOURCE_NAME + "2");

        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.addResourceDefinition(
            resource2.getServiceName(), resource2.getResourceType(), resource2.getResourceName());
        importerService.grantDefaultPermission(createDefaultPermissionGrant(CREATE_PERMISSION, resource));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(CREATE_PERMISSION, resource2));

        List<ResourceDefinition> result = service.resourceCreationAllowedList(Collections.singleton(ROLE_NAME));

        assertThat(result).containsExactly(resource2, resource);
    }

    //TODO: grant default permissions for different roles and test calling method for [Role 1, Role2]
    //    @Test
    //    void shouldRetrieveMultipleResourceDefinitionsWhenDefaultPermissionsExistForDifferentRoles() {
    //    }

    private ResourceDefinition resourceBuilder(String resourceName) {
        return ResourceDefinition.builder()
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(resourceName)
            .build();
    }
}
