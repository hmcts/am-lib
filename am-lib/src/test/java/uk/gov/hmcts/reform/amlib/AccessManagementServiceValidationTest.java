package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.amlib.helpers.InvalidArgumentsProvider;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

@SuppressWarnings({"PMD", "LineLength"})
class AccessManagementServiceValidationTest {
    private final AccessManagementService service = new AccessManagementService("", "", "");

    @Disabled("TODO: implement")
    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void grantExplicitResourceAccessMethodShouldRejectInvalidArguments(ExplicitAccessGrant explicitAccessGrant) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.grantExplicitResourceAccess(explicitAccessGrant))
            .withMessageMatching("");
    }

    @Disabled("TODO: implement")
    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void revokeResourceAccessMethodShouldRejectInvalidArguments(ExplicitAccessMetadata explicitAccessMetadata) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.revokeResourceAccess(explicitAccessMetadata))
            .withMessageMatching("");
    }

    @Disabled("TODO: implement")
    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void filterResourceMethodShouldRejectInvalidArguments(String userId, Set<String> userRoles, List<Resource> resources) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.filterResource(userId, userRoles, resources))
            .withMessageMatching("");
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentsForFilterResourceMethod")
    void filterResourceMethodShouldRejectInvalidArguments(String userId, Set<String> userRoles, Resource resource) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.filterResource(userId, userRoles, resource))
            .withMessageMatching(".*(userId|userRoles|userRoles\\[\\].<iterable element>|resource|resource.resourceId|resource.type|resource.type.serviceName|resource.type.resourceType|resource.type.resourceName|resource.resourceJson) - must not be (null|blank|empty)");
    }

    private static Stream<Arguments> invalidArgumentsForFilterResourceMethod() {
        return Stream.of(
            Arguments.of(null, null, null),
            Arguments.of("", ImmutableSet.of(), validResourceBuilder().build()),
            Arguments.of(" ", ImmutableSet.of("citizen"), validResourceBuilder().build()),
            Arguments.of(ACCESSOR_ID, null, validResourceBuilder().build()),
            Arguments.of(ACCESSOR_ID, ImmutableSet.of(), validResourceBuilder().build()),
            Arguments.of(ACCESSOR_ID, ImmutableSet.of(""), validResourceBuilder().build()),
            Arguments.of(ACCESSOR_ID, ImmutableSet.of(" "), validResourceBuilder().build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, null),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().resourceId(null).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().resourceId("").build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().resourceId(" ").build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().type(null).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().type(validResourceDefinitionBuilder().serviceName(null).build()).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().type(validResourceDefinitionBuilder().serviceName("").build()).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().type(validResourceDefinitionBuilder().serviceName(" ").build()).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().type(validResourceDefinitionBuilder().resourceType(null).build()).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().type(validResourceDefinitionBuilder().resourceType("").build()).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().type(validResourceDefinitionBuilder().resourceType(" ").build()).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().type(validResourceDefinitionBuilder().resourceName(null).build()).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().type(validResourceDefinitionBuilder().resourceName("").build()).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().type(validResourceDefinitionBuilder().resourceName(" ").build()).build()),
            Arguments.of(ACCESSOR_ID, ROLE_NAMES, validResourceBuilder().resourceJson(null).build())
        );
    }

    private static Resource.ResourceBuilder validResourceBuilder() {
        return Resource.builder()
            .resourceId("R1")
            .type(validResourceDefinitionBuilder().build())
            .resourceJson(JsonNodeFactory.instance.objectNode());
    }

    private static ResourceDefinition.ResourceDefinitionBuilder validResourceDefinitionBuilder() {
        return ResourceDefinition.builder()
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME);
    }

    @ParameterizedTest
    @ArgumentsSource(InvalidArgumentsProvider.class)
    void getRolePermissionsMethodShouldRejectInvalidArguments(String serviceName, String resourceType, String resourceName, Set<String> roleNames) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(serviceName, resourceType, resourceName, roleNames))
            .withMessageMatching(".*(serviceName|resourceType|resourceName|roleNames|roleNames\\[\\].<iterable element>) - must not be (null|blank|empty)");
    }
}
