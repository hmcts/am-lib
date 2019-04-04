package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.ValidationMessageRegexFactory.expectedValidationMessagesRegex;

@SuppressWarnings({"PMD.LinguisticNaming", "PMD.AvoidDuplicateLiterals"})
class DefaultRoleSetupImportServiceValidationTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addServiceMethodShouldRejectInvalidArguments(String serviceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addService(serviceName))
            .withMessageMatching(expectedValidationMessagesRegex(
                "serviceName - must not be blank"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addRoleMethodShouldRejectInvalidArguments(String roleName,
                                                   RoleType roleType,
                                                   SecurityClassification securityClassification,
                                                   AccessManagementType accessType) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(roleName, roleType, securityClassification, accessType))
            .withMessageMatching(expectedValidationMessagesRegex(
                "roleName - must not be blank",
                "roleType - must not be null",
                "securityClassification - must not be null",
                "accessManagementType - must not be null"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addResourceDefinitionMethodShouldRejectInvalidArguments(String serviceName,
                                                                 String resourceType,
                                                                 String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching(expectedValidationMessagesRegex(
                "serviceName - must not be blank",
                "resourceType - must not be blank",
                "resourceName - must not be blank"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void grantDefaultPermissionMethodShouldRejectInvalidArguments(DefaultPermissionGrant accessGrant) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.grantDefaultPermission(accessGrant))
            .withMessageMatching(expectedValidationMessagesRegex(
                "accessGrant - must not be null",
                "accessGrant.serviceName - must not be blank",
                "accessGrant.resourceType - must not be blank",
                "accessGrant.resourceName - must not be blank",
                "accessGrant.roleName - must not be blank",
                "accessGrant.attributePermissions - must not be empty"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void truncateDefaultPermissionsForServiceMethodShouldRejectInvalidArguments(String serviceName,
                                                                                String resourceType) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.truncateDefaultPermissionsForService(serviceName, resourceType))
            .withMessageMatching(expectedValidationMessagesRegex(
                "serviceName - must not be blank",
                "resourceType - must not be blank"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    @SuppressWarnings("LineLength")
    void truncateDefaultPermissionsByResourceDefinitionMethodShouldRejectInvalidArguments(String serviceName,
                                                                                          String resourceType,
                                                                                          String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.truncateDefaultPermissionsByResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching(expectedValidationMessagesRegex(
                "serviceName - must not be blank",
                "resourceType - must not be blank",
                "resourceName - must not be blank"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteResourceDefinitionMethodShouldRejectInvalidArguments(String serviceName,
                                                                    String resourceType,
                                                                    String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching(expectedValidationMessagesRegex(
                "serviceName - must not be blank",
                "resourceType - must not be blank",
                "resourceName - must not be blank"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteRoleMethodShouldRejectInvalidArguments(String roleName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteRole(roleName))
            .withMessageMatching(expectedValidationMessagesRegex(
                "roleName - must not be blank"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteServiceMethodShouldRejectInvalidArguments(String serviceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteService(serviceName))
            .withMessageMatching(expectedValidationMessagesRegex(
                "serviceName - must not be blank"
            ));
    }

}
