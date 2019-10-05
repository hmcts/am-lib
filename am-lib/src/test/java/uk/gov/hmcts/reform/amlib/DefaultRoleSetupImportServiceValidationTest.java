package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.service.DefaultRoleSetupImportService;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.ValidationMessageRegexFactory.expectedValidationMessagesRegex;

@SuppressWarnings({"PMD.LinguisticNaming", "PMD.AvoidDuplicateLiterals"})
@Disabled
class DefaultRoleSetupImportServiceValidationTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportServiceImpl("", "", "");

    //@ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addServiceMethodShouldRejectInvalidArguments(String serviceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addService(serviceName))
            .withMessageMatching(expectedValidationMessagesRegex(
                "serviceName - must not be blank"
            ));
    }

    //@ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addRoleMethodShouldRejectInvalidArguments(String roleName,
                                                   RoleType roleType,
                                                   SecurityClassification securityClassification,
                                                   AccessType accessType) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(roleName, roleType, securityClassification, accessType))
            .withMessageMatching(expectedValidationMessagesRegex(
                "roleName - must not be blank",
                "roleType - must not be null",
                "securityClassification - must not be null",
                "accessType - must not be null"
            ));
    }

    //@ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void addResourceDefinitionMethodShouldRejectInvalidArguments(ResourceDefinition resourceDefinition) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(resourceDefinition))
            .withMessageMatching(expectedValidationMessagesRegex(
                "resourceDefinition - must not be null",
                "resourceDefinition.serviceName - must not be blank",
                "resourceDefinition.resourceType - must not be blank",
                "resourceDefinition.resourceName - must not be blank"
            ));
    }

    //@ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void grantDefaultPermissionMethodShouldRejectInvalidArguments(DefaultPermissionGrant accessGrant) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.grantDefaultPermission(accessGrant))
            .withMessageMatching(expectedValidationMessagesRegex(
                "accessGrant - must not be null",
                "accessGrant.resourceDefinition - must not be null",
                "accessGrant.resourceDefinition.serviceName - must not be blank",
                "accessGrant.resourceDefinition.resourceType - must not be blank",
                "accessGrant.resourceDefinition.resourceName - must not be blank",
                "accessGrant.roleName - must not be blank",
                "accessGrant.attributePermissions - must not be empty"
            ));
    }

    //@ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void truncateDefaultPermissionsForServiceMethodShouldRejectInvalidArguments(String serviceName,
                                                                                String resourceType) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.truncateDefaultPermissionsForService(serviceName, resourceType,
                null, null))
            .withMessageMatching(expectedValidationMessagesRegex(
                "serviceName - must not be blank",
                "resourceType - must not be blank"
            ));
    }

    //@ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    @SuppressWarnings("LineLength")
    void truncateDefaultPermissionsByResourceDefinitionMethodShouldRejectInvalidArguments(ResourceDefinition resourceDefinition) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.truncateDefaultPermissionsByResourceDefinition(resourceDefinition,
                null, null))
            .withMessageMatching(expectedValidationMessagesRegex(
                "resourceDefinition - must not be null",
                "resourceDefinition.serviceName - must not be blank",
                "resourceDefinition.resourceType - must not be blank",
                "resourceDefinition.resourceName - must not be blank"
            ));
    }

    //@ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteResourceDefinitionMethodShouldRejectInvalidArguments(ResourceDefinition resourceDefinition) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteResourceDefinition(resourceDefinition))
            .withMessageMatching(expectedValidationMessagesRegex(
                "resourceDefinition - must not be null",
                "resourceDefinition.serviceName - must not be blank",
                "resourceDefinition.resourceType - must not be blank",
                "resourceDefinition.resourceName - must not be blank"
            ));
    }

    //@ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteRoleMethodShouldRejectInvalidArguments(String roleName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteRole(roleName))
            .withMessageMatching(expectedValidationMessagesRegex(
                "roleName - must not be blank"
            ));
    }

    //@ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void deleteServiceMethodShouldRejectInvalidArguments(String serviceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteService(serviceName))
            .withMessageMatching(expectedValidationMessagesRegex(
                "serviceName - must not be blank"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void returnRolePermissionsForCaseTypeShouldRejectInvalidArguments(String caseTypeId) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissionsForCaseType(caseTypeId))
            .withMessageMatching(expectedValidationMessagesRegex(
                "caseTypeId - must not be blank"
            ));
    }
}
