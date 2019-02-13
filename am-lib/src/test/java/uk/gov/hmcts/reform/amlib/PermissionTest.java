package uk.gov.hmcts.reform.amlib;

import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;
import uk.gov.hmcts.reform.amlib.utils.Permissions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.Java6Assertions.assertThat;

@SuppressWarnings("PMD")
public class PermissionTest {

    @Test
    public void sumOf_whenPassingPermissions_theSumOfValuesIsCalculated() {
        Set<Permission> permissions = new HashSet<>(Arrays.asList(Permission.CREATE, Permission.READ));

        int sum = Permissions.sumOf(permissions);

        int expectedSum = Permission.CREATE.getValue() + Permission.READ.getValue();
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
    public void fromSumOf_sumOfPermissionsValueSeven_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(7);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ, Permission.UPDATE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueTen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(10);

        assertThat(permissions).containsOnly(Permission.SHARE, Permission.READ);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueThirteen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(13);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.UPDATE, Permission.SHARE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueFifteen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(15);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ, Permission.UPDATE, Permission.SHARE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueSeventeen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(17);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.DELETE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueTwentyTwo_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(22);

        assertThat(permissions).containsOnly(Permission.READ, Permission.DELETE, Permission.UPDATE);
    }


    @Test
    public void fromSumOf_sumOfPermissionsValueTwentyFive_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(25);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.SHARE, Permission.DELETE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueTwentyEight_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(28);

        assertThat(permissions).containsOnly(Permission.SHARE, Permission.DELETE, Permission.UPDATE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueThirtyOne_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.fromSumOf(31);

        assertThat(permissions).containsOnly(
            Permission.CREATE, Permission.READ, Permission.UPDATE, Permission.SHARE, Permission.DELETE);
    }

    @Test
    public void fromSumOf_sumOfPermissionsValueHighValue_ShouldThrowUnsupportedPermission() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permissions.fromSumOf(32))
            .withMessage("The given permissions are not supported");
    }

    @Test
    public void fromSumOf_sumOfPermissionsNegativeValue_ExpectArray() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permissions.fromSumOf(-5))
            .withMessage("The given permissions are not supported");
    }
}
