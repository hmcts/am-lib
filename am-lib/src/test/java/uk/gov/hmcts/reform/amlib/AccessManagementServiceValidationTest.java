package uk.gov.hmcts.reform.amlib;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

@SuppressWarnings({"PMD", "LineLength"})
class AccessManagementServiceValidationTest {
    private final AccessManagementService service = new AccessManagementService("", "", "");

    @ParameterizedTest
    @MethodSource("invalidArgumentsForGetRolePermissionsMethod")
    void getRolePermissionsMethodShouldRejectInvalidArguments(String serviceName, String resourceType, String resourceName, Set<String> roleNames) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.getRolePermissions(serviceName, resourceType, resourceName, roleNames))
            .withMessageMatching(".*(serviceName|resourceType|resourceName|roleNames|roleNames\\[\\].<iterable element>) - must not be (null|blank|empty)");
    }

    private static Stream<Arguments> invalidArgumentsForGetRolePermissionsMethod() {
        return Stream.of(
            Arguments.of(null, RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAMES),
            Arguments.of("", RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAMES),
            Arguments.of(" ", RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAMES),
            Arguments.of(SERVICE_NAME, null, RESOURCE_NAME, ROLE_NAMES),
            Arguments.of(SERVICE_NAME, "", RESOURCE_NAME, ROLE_NAMES),
            Arguments.of(SERVICE_NAME, " ", RESOURCE_NAME, ROLE_NAMES),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, null, ROLE_NAMES),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, "", ROLE_NAMES),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, " ", ROLE_NAMES),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, null),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ImmutableSet.of()),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ImmutableSet.of("")),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME, ImmutableSet.of(" "))
        );
    }
}
