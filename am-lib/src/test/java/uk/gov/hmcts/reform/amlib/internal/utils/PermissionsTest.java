package uk.gov.hmcts.reform.amlib.internal.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;

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
class PermissionsTest {

    @Test
    void sumOf_shouldCalculateSumOfPermissionsFromEmptyArray() {
        int sumOfPermissions = Permissions.sumOf();
        assertThat(sumOfPermissions).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("createArguments")
    void sumOf_shouldCalculateSumOfPermissionsFromArray(Arguments args) {
        int sumOfPermissions = Permissions.sumOf(args.permissions);
        assertThat(sumOfPermissions).isEqualTo(args.sumOfPermissions);
    }

    @Test
    void sumOf_shouldCalculateSumOfPermissionsFromEmptySet() {
        int sumOfPermissions = Permissions.sumOf(new HashSet<>());
        assertThat(sumOfPermissions).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("createArguments")
    void sumOf_shouldCalculateSumOfPermissionsFromSet(Arguments args) {
        int sumOfPermissions = Permissions.sumOf(Arrays.stream(args.permissions).collect(Collectors.toSet()));
        assertThat(sumOfPermissions).isEqualTo(args.sumOfPermissions);
    }

    @ParameterizedTest
    @MethodSource("createArguments")
    void fromSumOf_shouldDeductSetOfPermissionsFromNumericSum(Arguments args) {
        Set<Permission> permissions = Permissions.fromSumOf(args.sumOfPermissions);
        assertThat(permissions).containsOnly(args.permissions);
    }

    @Test
    void fromSumOf_shouldThrowExceptionWhenPermissionSumIsGreaterThenMaxValue() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permissions.fromSumOf(Permissions.MAX_PERMISSIONS_VALUE + 1))
            .withMessage("The given permissions are not supported");
    }

    @Test
    void fromSumOf_shouldThrowExceptionWhenPermissionSumIsLesserThenMinValue() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permissions.fromSumOf(Permissions.MIN_PERMISSIONS_VALUE - 5))
            .withMessage("The given permissions are not supported");
    }

    private static Stream<Arguments> createArguments() {
        return Stream.of(
            new Arguments(1, CREATE),
            new Arguments(2, READ),
            new Arguments(3, CREATE, READ),
            new Arguments(4, UPDATE),
            new Arguments(5, CREATE, UPDATE),
            new Arguments(6, READ, UPDATE),
            new Arguments(7, CREATE, READ, UPDATE),
            new Arguments(8, DELETE),
            new Arguments(9, CREATE, DELETE),
            new Arguments(10, READ, DELETE),
            new Arguments(11, CREATE, READ, DELETE),
            new Arguments(12, UPDATE, DELETE),
            new Arguments(13, CREATE, UPDATE, DELETE),
            new Arguments(14, READ, UPDATE, DELETE),
            new Arguments(15, CREATE, READ, UPDATE, DELETE)
        );
    }

    private static class Arguments {
        private final int sumOfPermissions;
        private final Permission[] permissions;

        private Arguments(int sumOfPermissions, Permission... permissions) {
            this.sumOfPermissions = sumOfPermissions;
            this.permissions = permissions;
        }
    }
}
