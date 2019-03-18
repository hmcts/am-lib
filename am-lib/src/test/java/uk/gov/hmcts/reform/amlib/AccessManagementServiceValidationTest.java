package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SuppressWarnings({"PMD", "LineLength"})
class AccessManagementServiceValidationTest {
    private final AccessManagementService service = new AccessManagementService("", "", "");

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void grantExplicitResourceAccessMethodShouldRejectInvalidArguments(ExplicitAccessGrant explicitAccessGrant) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.grantExplicitResourceAccess(explicitAccessGrant))
            .withMessageMatching("[^;]+(explicitAccessGrant|explicitAccessGrant.resourceId|explicitAccessGrant.accessorId|explicitAccessGrant.accessType|explicitAccessGrant.serviceName|explicitAccessGrant.resourceType|explicitAccessGrant.resourceName|explicitAccessGrant.attributePermissions|explicitAccessGrant.securityClassification) - must not be (null|blank|empty)");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void revokeResourceAccessMethodShouldRejectInvalidArguments(ExplicitAccessMetadata explicitAccessMetadata) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.revokeResourceAccess(explicitAccessMetadata))
            .withMessageMatching("[^;]+(explicitAccessMetadata|explicitAccessMetadata.resourceId|explicitAccessMetadata.accessorId|explicitAccessMetadata.accessType|explicitAccessMetadata.serviceName|explicitAccessMetadata.resourceType|explicitAccessMetadata.resourceName|explicitAccessMetadata.attribute|explicitAccessMetadata.securityClassification) - must not be (null|blank)");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void filterResourceMethodShouldRejectInvalidArguments(String userId, Set<String> userRoles, List<Resource> resources) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.filterResource(userId, userRoles, resources))
            .withMessageMatching("[^;]+(userId|userRoles|userRoles\\[\\].<iterable element>|resources|resources\\[0\\].resourceId|resources\\[0\\].type|resources\\[0\\].type.serviceName|resources\\[0\\].type.resourceType|resources\\[0\\].type.resourceName|resources\\[0\\].resourceJson) - must not be (null|blank|empty)");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void filterResourceMethodShouldRejectInvalidArguments(String userId, Set<String> userRoles, Resource resource) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.filterResource(userId, userRoles, resource))
            .withMessageMatching("[^;]+(userId|userRoles|userRoles\\[\\].<iterable element>|resource|resource.resourceId|resource.type|resource.type.serviceName|resource.type.resourceType|resource.type.resourceName|resource.resourceJson) - must not be (null|blank|empty)");
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void getRolePermissionsMethodShouldRejectInvalidArguments(String serviceName, String resourceType, String resourceName, Set<String> roleNames) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(serviceName, resourceType, resourceName, roleNames))
            .withMessageMatching("[^;]+(serviceName|resourceType|resourceName|roleNames|roleNames\\[\\].<iterable element>) - must not be (null|blank|empty)");
    }
}
