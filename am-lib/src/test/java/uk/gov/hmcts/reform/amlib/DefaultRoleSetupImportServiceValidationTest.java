package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.ValidationMessageRegexFactory.validationMessageRegex;

@SuppressWarnings({"PMD"})
class DefaultRoleSetupImportServiceValidationTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addServiceMethodShouldRejectInvalidArguments(String serviceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addService(serviceName))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of("serviceName"), "blank"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addRoleMethodShouldRejectInvalidArguments(String roleName,
                                                   RoleType roleType,
                                                   SecurityClassification securityClassification,
                                                   AccessType accessType) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(roleName, roleType, securityClassification, accessType))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of(
                "roleName",
                "roleType",
                "securityClassification",
                "accessType"
            ), "null|blank"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addResourceDefinitionMethodShouldRejectInvalidArguments(String serviceName,
                                                                 String resourceType,
                                                                 String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of(
                "serviceName",
                "resourceType",
                "resourceName"
            ), "blank"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void grantDefaultPermissionMethodShouldRejectInvalidArguments(DefaultPermissionGrant defaultPermissionGrant) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.grantDefaultPermission(defaultPermissionGrant))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of(
                "defaultPermissionGrant",
                "defaultPermissionGrant.serviceName",
                "defaultPermissionGrant.resourceType",
                "defaultPermissionGrant.resourceName",
                "defaultPermissionGrant.roleName",
                "defaultPermissionGrant.attributePermissions"
            ), "null|blank|empty"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void truncateDefaultPermissionsForServiceMethodShouldRejectInvalidArguments(String serviceName,
                                                                                String resourceType) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.truncateDefaultPermissionsForService(serviceName, resourceType))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of(
                "serviceName",
                "resourceType"
            ), "blank"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    @SuppressWarnings("LineLength")
    void truncateDefaultPermissionsByResourceDefinitionMethodShouldRejectInvalidArguments(String serviceName,
                                                                                          String resourceType,
                                                                                          String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.truncateDefaultPermissionsByResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of(
                "serviceName",
                "resourceType",
                "resourceName"
            ), "blank"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteResourceDefinitionMethodShouldRejectInvalidArguments(String serviceName,
                                                                    String resourceType,
                                                                    String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of(
                "serviceName",
                "resourceType",
                "resourceName"
            ), "blank"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteRoleMethodShouldRejectInvalidArguments(String roleName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteRole(roleName))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of("roleName"), "blank"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteServiceMethodShouldRejectInvalidArguments(String serviceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteService(serviceName))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of("serviceName"), "blank"));
    }

}
