package uk.gov.hmcts.reform.amlib;

import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.Java6Assertions.assertThat;

@SuppressWarnings("PMD")
public class PermissionTest {

    @Test
    public void sumOf_whenPassingSetOfPermissions_theSumOfValuesIsCalculated() {
        Set<Permission> permissions = Stream.of(Permission.CREATE, Permission.READ).collect(Collectors.toSet());

        int sum = Permissions.sumOf(permissions);
        int expectedSum = Permission.CREATE.getValue() + Permission.READ.getValue();

        assertThat(sum).isEqualTo(expectedSum);
    }

    @Test
    public void sumOf_whenPassingAPermission_theSumOfValuesIsCalculated() {
        int sum = Permissions.sumOf(Permission.CREATE);
        int expectedSum = Permission.CREATE.getValue();

        assertThat(sum).isEqualTo(expectedSum);
    }

    @Test
    public void sumOf_whenPassingNoPermissions_theSumOfValuesIsZero() {
        Set<Permission> permissions = new HashSet<>();

        int sum = Permissions.sumOf(permissions);

        assertThat(sum).isEqualTo(0);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueOne_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(1);

        assertThat(permissions).containsOnly(Permission.CREATE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueTwo_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(2);

        assertThat(permissions).containsOnly(Permission.READ);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueThree_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(3);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueFour_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(4);

        assertThat(permissions).containsOnly(Permission.UPDATE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueFive_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(5);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.UPDATE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueSix_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(6);

        assertThat(permissions).containsOnly(Permission.READ, Permission.UPDATE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueSeven_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(7);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ, Permission.UPDATE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueEight_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(8);

        assertThat(permissions).containsOnly(Permission.DELETE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueNine_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(9);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.DELETE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueTen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(10);

        assertThat(permissions).containsOnly(Permission.READ, Permission.DELETE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueEleven_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(11);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ, Permission.DELETE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueTwelve_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(12);

        assertThat(permissions).containsOnly(Permission.DELETE, Permission.UPDATE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueThirteen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(13);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.UPDATE, Permission.DELETE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueFourteen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(14);

        assertThat(permissions).containsOnly(Permission.READ, Permission.DELETE, Permission.UPDATE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueFifteen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(15);

        assertThat(permissions).containsOnly(
            Permission.CREATE, Permission.READ, Permission.UPDATE, Permission.DELETE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueHighValue_ShouldThrowUnsupportedPermission() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permissions.fromSumOf(Permissions.MAX_PERMISSIONS_VALUE + 1))
            .withMessage("The given permissions are not supported");
    }

    @Test
    public void fromSumOf_sumOfPermissionsNegativeValue_ExpectArray() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permissions.fromSumOf(Permissions.MIN_PERMISSIONS_VALUE - 5))
            .withMessage("The given permissions are not supported");
    }
}
