package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SuppressWarnings({"PMD", "LineLength"})
class DefaultRoleSetupImportServiceValidationTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addServiceMethodShouldRejectInvalidArguments(String serviceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addService(serviceName))
            .withMessageMatching("[^;]+serviceName - must not be blank");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addRoleMethodShouldRejectInvalidArguments(String roleName, RoleType roleType, SecurityClassification securityClassification, AccessType accessType) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(roleName, roleType, securityClassification, accessType))
            .withMessageMatching("[^;]+(roleName|roleType|securityClassification|accessType) - must not be (null|blank)");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addResourceDefinitionMethodShouldRejectInvalidArguments(String serviceName, String resourceType, String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching("[^;]+(serviceName|resourceType|resourceName) - must not be blank");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void grantDefaultPermissionMethodShouldRejectInvalidArguments(DefaultPermissionGrant defaultPermissionGrant) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.grantDefaultPermission(defaultPermissionGrant))
            .withMessageMatching("[^;]+(defaultPermissionGrant|defaultPermissionGrant.serviceName|defaultPermissionGrant.resourceType|defaultPermissionGrant.resourceName|defaultPermissionGrant.roleName|defaultPermissionGrant.attributePermissions) - must not be (null|blank|empty)");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void truncateDefaultPermissionsForServiceMethodShouldRejectInvalidArguments(String serviceName, String resourceType) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.truncateDefaultPermissionsForService(serviceName, resourceType))
            .withMessageMatching("[^;]+(serviceName|resourceType) - must not be blank");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void truncateDefaultPermissionsByResourceDefinitionMethodShouldRejectInvalidArguments(String serviceName, String resourceType, String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.truncateDefaultPermissionsByResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching("[^;]+(serviceName|resourceType|resourceName) - must not be blank");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteResourceDefinitionMethodShouldRejectInvalidArguments(String serviceName, String resourceType, String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching("[^;]+(serviceName|resourceType|resourceName) - must not be blank");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteRoleMethodShouldRejectInvalidArguments(String roleName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteRole(roleName))
            .withMessageMatching("[^;]+(roleName) - must not be blank");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteServiceMethodShouldRejectInvalidArguments(String serviceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteService(serviceName))
            .withMessageMatching("[^;]+serviceName - must not be blank");
    }

}
