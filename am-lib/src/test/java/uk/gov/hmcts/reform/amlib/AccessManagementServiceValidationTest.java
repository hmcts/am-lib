package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.ValidationMessageRegexFactory.expectedValidationMessagesRegex;

@SuppressWarnings("PMD.LinguisticNaming")
class AccessManagementServiceValidationTest {
    private final AccessManagementService service = new AccessManagementService("", "", "");

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void grantExplicitResourceAccessMethodShouldRejectInvalidArguments(ExplicitAccessGrant accessGrant) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.grantExplicitResourceAccess(accessGrant))
            .withMessageMatching(expectedValidationMessagesRegex(
                "accessGrant - must not be null",
                "accessGrant.resourceId - must not be blank",
                "accessGrant.accessorIds - must not be empty",
                "accessGrant.accessType - must not be blank",
                "accessGrant.serviceName - must not be blank",
                "accessGrant.resourceType - must not be blank",
                "accessGrant.resourceName - must not be blank",
                "accessGrant.attributePermissions - must not be empty",
                "accessGrant.securityClassification - must not be null"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void revokeResourceAccessMethodShouldRejectInvalidArguments(ExplicitAccessMetadata accessMetadata) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.revokeResourceAccess(accessMetadata))
            .withMessageMatching(expectedValidationMessagesRegex(
                "accessMetadata - must not be null",
                "accessMetadata.resourceId - must not be blank",
                "accessMetadata.accessorId - must not be blank",
                "accessMetadata.accessType - must not be blank",
                "accessMetadata.serviceName - must not be blank",
                "accessMetadata.resourceType - must not be blank",
                "accessMetadata.resourceName - must not be blank",
                "accessMetadata.attribute - must not be null",
                "accessMetadata.securityClassification - must not be null"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void filterResourceMethodShouldRejectInvalidArguments(String userId, Set<String> userRoles, Resource resource) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.filterResource(userId, userRoles, resource))
            .withMessageMatching(expectedValidationMessagesRegex(
                "userId - must not be blank",
                "userRoles - must not be empty",
                "userRoles\\[\\].<iterable element> - must not be blank",
                "resource - must not be null",
                "resource.id - must not be blank",
                "resource.definition - must not be null",
                "resource.definition.serviceName - must not be blank",
                "resource.definition.resourceType - must not be blank",
                "resource.definition.resourceName - must not be blank",
                "resource.data - must not be null"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void getRolePermissionsMethodShouldRejectInvalidArguments(ResourceDefinition resourceDefinition,
                                                              Set<String> roleNames) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(resourceDefinition, roleNames))
            .withMessageMatching(expectedValidationMessagesRegex(
                "resourceDefinition - must not be null",
                "resourceDefinition.serviceName - must not be blank",
                "resourceDefinition.resourceType - must not be blank",
                "resourceDefinition.resourceName - must not be blank",
                "userRoles - must not be empty",
                "userRoles\\[\\].<iterable element> - must not be blank"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void getResourceDefinitionsWithRootCreatePermissionMethodShouldRejectInvalidArguments(Set<String> userRoles) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getResourceDefinitionsWithRootCreatePermission(userRoles))
            .withMessageMatching(expectedValidationMessagesRegex(
                "userRoles - must not be empty",
                "userRoles\\[\\].<iterable element> - must not be blank"
            ));
    }
}
