package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.ValidationMessageRegexFactory.validationMessageRegex;

@SuppressWarnings({"PMD"})
class AccessManagementServiceValidationTest {
    private final AccessManagementService service = new AccessManagementService("", "", "");

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void grantExplicitResourceAccessMethodShouldRejectInvalidArguments(ExplicitAccessGrant explicitAccessGrant) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.grantExplicitResourceAccess(explicitAccessGrant))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of(
                "explicitAccessGrant",
                "explicitAccessGrant.resourceId",
                "explicitAccessGrant.accessorId",
                "explicitAccessGrant.accessType",
                "explicitAccessGrant.serviceName",
                "explicitAccessGrant.resourceType",
                "explicitAccessGrant.resourceName",
                "explicitAccessGrant.attributePermissions",
                "explicitAccessGrant.securityClassification"
            ), "null|blank|empty"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void revokeResourceAccessMethodShouldRejectInvalidArguments(ExplicitAccessMetadata explicitAccessMetadata) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.revokeResourceAccess(explicitAccessMetadata))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of(
                "explicitAccessMetadata",
                "explicitAccessMetadata.resourceId",
                "explicitAccessMetadata.accessorId",
                "explicitAccessMetadata.accessType",
                "explicitAccessMetadata.serviceName",
                "explicitAccessMetadata.resourceType",
                "explicitAccessMetadata.resourceName",
                "explicitAccessMetadata.attribute",
                "explicitAccessMetadata.securityClassification"
            ), "null|blank"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void filterResourceMethodShouldRejectInvalidArguments(String userId, Set<String> userRoles, Resource resource) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.filterResource(userId, userRoles, resource))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of("userId",
                "userRoles",
                "userRoles\\[\\].<iterable element>",
                "resource",
                "resource.resourceId",
                "resource.type",
                "resource.type.serviceName",
                "resource.type.resourceType",
                "resource.type.resourceName",
                "resource.resourceJson"), "null|blank|empty"));
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void getRolePermissionsMethodShouldRejectInvalidArguments(String serviceName,
                                                              String resourceType,
                                                              String resourceName,
                                                              Set<String> roleNames) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(serviceName, resourceType, resourceName, roleNames))
            .withMessageMatching(validationMessageRegex(ImmutableSet.of(
                "serviceName",
                "resourceType",
                "resourceName",
                "roleNames",
                "roleNames\\[\\].<iterable element>"
            ), "null|blank|empty"));
    }
}
