package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Map;
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
                "accessGrant.accessorType - must not be null",
                "accessGrant.resourceDefinition - must not be null",
                "accessGrant.resourceDefinition.serviceName - must not be blank",
                "accessGrant.resourceDefinition.resourceType - must not be blank",
                "accessGrant.resourceDefinition.resourceName - must not be blank",
                "accessGrant.attributePermissions - must not be empty",
                "accessGrant.relationship - must not be blank"
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
                "accessMetadata.accessorType - must not be null",
                "accessMetadata.resourceDefinition - must not be null",
                "accessMetadata.resourceDefinition.serviceName - must not be blank",
                "accessMetadata.resourceDefinition.resourceType - must not be blank",
                "accessMetadata.resourceDefinition.resourceName - must not be blank",
                "accessMetadata.attribute - must not be null",
                "accessMetadata.relationship - must not be blank"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void filterResourceMethodShouldRejectInvalidArguments(
        String userId, Set<String> userRoles, Resource resource,
        Map<JsonPointer, SecurityClassification> attributeSecurityClassifications) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.filterResource(userId, userRoles, resource, attributeSecurityClassifications))
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
                "resource.data - must not be null",
                "attributeSecurityClassification - must not be empty"
            ));
    }


    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void getRolePermissionsMethodShouldRejectInvalidArguments(ResourceDefinition resourceDefinition, String roleName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(resourceDefinition, roleName))
            .withMessageMatching(expectedValidationMessagesRegex(
                "resourceDefinition - must not be null",
                "resourceDefinition.serviceName - must not be blank",
                "resourceDefinition.resourceType - must not be blank",
                "resourceDefinition.resourceName - must not be blank",
                "roleName - must not be blank"
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
