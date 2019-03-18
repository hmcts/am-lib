package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.ValidationMessageRegexFactory.expectedValidationMessagesRegex;

@SuppressWarnings({"PMD"})
class AccessManagementServiceValidationTest {
    private final AccessManagementService service = new AccessManagementService("", "", "");

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void grantExplicitResourceAccessMethodShouldRejectInvalidArguments(ExplicitAccessGrant explicitAccessGrant) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.grantExplicitResourceAccess(explicitAccessGrant))
            .withMessageMatching(expectedValidationMessagesRegex(
                "explicitAccessGrant - must not be null",
                "explicitAccessGrant.resourceId - must not be blank",
                "explicitAccessGrant.accessorId - must not be blank",
                "explicitAccessGrant.accessType - must not be blank",
                "explicitAccessGrant.serviceName - must not be blank",
                "explicitAccessGrant.resourceType - must not be blank",
                "explicitAccessGrant.resourceName - must not be blank",
                "explicitAccessGrant.attributePermissions - must not be empty",
                "explicitAccessGrant.securityClassification - must not be null"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void revokeResourceAccessMethodShouldRejectInvalidArguments(ExplicitAccessMetadata explicitAccessMetadata) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.revokeResourceAccess(explicitAccessMetadata))
            .withMessageMatching(expectedValidationMessagesRegex(
                "explicitAccessMetadata - must not be null",
                "explicitAccessMetadata.resourceId - must not be blank",
                "explicitAccessMetadata.accessorId - must not be blank",
                "explicitAccessMetadata.accessType - must not be blank",
                "explicitAccessMetadata.serviceName - must not be blank",
                "explicitAccessMetadata.resourceType - must not be blank",
                "explicitAccessMetadata.resourceName - must not be blank",
                "explicitAccessMetadata.attribute - must not be null",
                "explicitAccessMetadata.securityClassification - must not be null"
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
                "resource.resourceId - must not be blank",
                "resource.type - must not be null",
                "resource.type.serviceName - must not be blank",
                "resource.type.resourceType - must not be blank",
                "resource.type.resourceName - must not be blank",
                "resource.resourceJson - must not be null"
            ));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void getRolePermissionsMethodShouldRejectInvalidArguments(String serviceName,
                                                              String resourceType,
                                                              String resourceName,
                                                              Set<String> roleNames) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(serviceName, resourceType, resourceName, roleNames))
            .withMessageMatching(expectedValidationMessagesRegex(
                "serviceName - must not be blank",
                "resourceType - must not be blank",
                "resourceName - must not be blank",
                "roleNames - must not be empty",
                "roleNames\\[\\].<iterable element> - must not be blank"
            ));
    }
}
