package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.DELETE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

@SuppressWarnings("PMD")
class PermissionTest {

    @Test
    void sumOf_whenPassingSetOfPermissions_theSumOfValuesIsCalculated() {
        Set<Permission> permissions = Stream.of(CREATE, Permission.READ).collect(Collectors.toSet());

        int sum = Permissions.sumOf(permissions);
        int expectedSum = CREATE.getValue() + Permission.READ.getValue();

        assertThat(sum).isEqualTo(expectedSum);
    }

    @Test
    void sumOf_whenPassingAPermission_theSumOfValuesIsCalculated() {
        int sum = Permissions.sumOf(CREATE);
        int expectedSum = CREATE.getValue();

        assertThat(sum).isEqualTo(expectedSum);
    }

    @Test
    void sumOf_whenPassingNoPermissions_theSumOfValuesIsZero() {
        Set<Permission> permissions = new HashSet<>();

        int sum = Permissions.sumOf(permissions);

        assertThat(sum).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("createPermissionTestData")
    void fromSumOf_shouldMapIntegerValueToSetOfPermissions(int sumOfPermissions, Set<Permission> permissions) {
        assertThat(Permissions.fromSumOf(sumOfPermissions))
            .containsOnlyElementsOf(permissions);
    }

    private static Stream<Arguments> createPermissionTestData() {
        return Stream.of(
            Arguments.of(1, setOf(CREATE)),
            Arguments.of(2, setOf(READ)),
            Arguments.of(3, setOf(CREATE, READ)),
            Arguments.of(4, setOf(UPDATE)),
            Arguments.of(5, setOf(CREATE, UPDATE)),
            Arguments.of(6, setOf(READ, UPDATE)),
            Arguments.of(7, setOf(CREATE, READ, UPDATE)),
            Arguments.of(8, setOf(DELETE)),
            Arguments.of(9, setOf(CREATE, DELETE)),
            Arguments.of(10, setOf(READ, DELETE)),
            Arguments.of(11, setOf(CREATE, READ, DELETE)),
            Arguments.of(12, setOf(UPDATE, DELETE)),
            Arguments.of(13, setOf(CREATE, UPDATE, DELETE)),
            Arguments.of(14, setOf(READ, UPDATE, DELETE)),
            Arguments.of(15, setOf(CREATE, READ, UPDATE, DELETE))
        );
    }

    @Test
    void fromSumOf_sumOfPermissionsValueHighValue_ShouldThrowUnsupportedPermission() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permissions.fromSumOf(Permissions.MAX_PERMISSIONS_VALUE + 1))
            .withMessage("The given permissions are not supported");
    }

    @Test
    void fromSumOf_sumOfPermissionsNegativeValue_ExpectArray() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permissions.fromSumOf(Permissions.MIN_PERMISSIONS_VALUE - 5))
            .withMessage("The given permissions are not supported");
    }

    private static Set<Permission> setOf(Permission... permissions) {
        return Arrays.stream(permissions).collect(Collectors.toSet());
    }
}
