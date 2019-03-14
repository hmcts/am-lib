package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

@SuppressWarnings({"PMD", "LineLength"})
class DefaultRoleSetupImportServiceValidationTest {
    private final DefaultRoleSetupImportService service = new DefaultRoleSetupImportService("", "", "");

    @ParameterizedTest
    @MethodSource("invalidArgumentsForAddServiceMethod")
    void addServiceMethodShouldRejectInvalidArguments(String serviceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addService(serviceName))
            .withMessageContaining("serviceName - must not be blank");
    }

    private static Stream<String> invalidArgumentsForAddServiceMethod() {
        return Stream.of(null, "", " ");
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentsForAddRoleMethod")
    void addRoleMethodShouldRejectInvalidArguments(String roleName, RoleType roleType, SecurityClassification securityClassification, AccessType accessType) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addRole(roleName, roleType, securityClassification, accessType))
            .withMessageMatching(".*(roleName|roleType|securityClassification|accessType) - must not be (null|blank)");
    }

    private static Stream<Arguments> invalidArgumentsForAddRoleMethod() {
        return Stream.of(
            Arguments.of(null, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED),
            Arguments.of("", RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED),
            Arguments.of(" ", RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED),
            Arguments.of(ROLE_NAME, null, SecurityClassification.PUBLIC, AccessType.ROLE_BASED),
            Arguments.of(ROLE_NAME, RoleType.RESOURCE, null, AccessType.ROLE_BASED),
            Arguments.of(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentsForAddResourceDefinitionMethod")
    void addResourceDefinitionShouldRejectInvalidArguments(String serviceName, String resourceType, String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.addResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching(".*(serviceName|resourceType|resourceName) - must not be blank");
    }

    private static Stream<Arguments> invalidArgumentsForAddResourceDefinitionMethod() {
        return Stream.of(
            Arguments.of(null, RESOURCE_TYPE, RESOURCE_NAME),
            Arguments.of("", RESOURCE_TYPE, RESOURCE_NAME),
            Arguments.of(" ", RESOURCE_TYPE, RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, null, RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, "", RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, " ", RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, null),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, ""),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, " ")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentsForTruncateDefaultPermissionsForServiceMethod")
    void truncateDefaultPermissionsForServiceShouldRejectInvalidArguments(String serviceName, String resourceType) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.truncateDefaultPermissionsForService(serviceName, resourceType))
            .withMessageMatching(".*(serviceName|resourceType) - must not be blank");
    }

    private static Stream<Arguments> invalidArgumentsForTruncateDefaultPermissionsForServiceMethod() {
        return Stream.of(
            Arguments.of(null, RESOURCE_TYPE),
            Arguments.of("", RESOURCE_TYPE),
            Arguments.of(" ", RESOURCE_TYPE),
            Arguments.of(SERVICE_NAME, null),
            Arguments.of(SERVICE_NAME, ""),
            Arguments.of(SERVICE_NAME, " ")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentsForTruncateDefaultPermissionsByResourceDefinitionMethod")
    void truncateDefaultPermissionsByResourceDefinitionShouldRejectInvalidArguments(String serviceName, String resourceType, String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.truncateDefaultPermissionsByResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching(".*(serviceName|resourceType|resourceName) - must not be blank");
    }

    private static Stream<Arguments> invalidArgumentsForTruncateDefaultPermissionsByResourceDefinitionMethod() {
        return Stream.of(
            Arguments.of(null, RESOURCE_TYPE, RESOURCE_NAME),
            Arguments.of("", RESOURCE_TYPE, RESOURCE_NAME),
            Arguments.of(" ", RESOURCE_TYPE, RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, null, RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, "", RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, " ", RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, null),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, ""),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, " ")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentsForDeleteResourceDefinitionMethod")
    void deleteResourceDefinitionShouldRejectInvalidArguments(String serviceName, String resourceType, String resourceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteResourceDefinition(serviceName, resourceType, resourceName))
            .withMessageMatching(".*(serviceName|resourceType|resourceName) - must not be blank");
    }

    private static Stream<Arguments> invalidArgumentsForDeleteResourceDefinitionMethod() {
        return Stream.of(
            Arguments.of(null, RESOURCE_TYPE, RESOURCE_NAME),
            Arguments.of("", RESOURCE_TYPE, RESOURCE_NAME),
            Arguments.of(" ", RESOURCE_TYPE, RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, null, RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, "", RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, " ", RESOURCE_NAME),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, null),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, ""),
            Arguments.of(SERVICE_NAME, RESOURCE_TYPE, " ")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentsForDeleteRoleMethod")
    void deleteRoleMethodShouldRejectInvalidArguments(String roleName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteRole(roleName))
            .withMessageMatching(".*(roleName) - must not be blank");
    }

    private static Stream<String> invalidArgumentsForDeleteRoleMethod() {
        return Stream.of(null, "", " ");
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentsForDeleteServiceMethod")
    void deleteServiceMethodShouldRejectInvalidArguments(String serviceName) {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> service.deleteService(serviceName))
            .withMessageContaining("serviceName - must not be blank");
    }

    private static Stream<String> invalidArgumentsForDeleteServiceMethod() {
        return Stream.of(null, "", " ");
    }

}
