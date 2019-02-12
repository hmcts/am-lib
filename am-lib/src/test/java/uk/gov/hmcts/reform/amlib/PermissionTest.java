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

    public void buildPermissions_sumOfPermissionsValueOne_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(1);

        assertThat(permissions).containsOnly(Permission.CREATE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueThree_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(3);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueFour_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(4);

        assertThat(permissions).containsOnly(Permission.UPDATE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueSeven_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(7);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ, Permission.UPDATE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueTen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(10);

        assertThat(permissions).containsOnly(Permission.SHARE, Permission.READ);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueThirteen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(13);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.UPDATE, Permission.SHARE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueFifteen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(15);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ, Permission.UPDATE, Permission.SHARE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueSeventeen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(17);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.DELETE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueTwentyTwo_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(22);

        assertThat(permissions).containsOnly(Permission.READ, Permission.DELETE, Permission.UPDATE);
    }


    @Test
    public void buildPermissions_sumOfPermissionsValueTwentyFive_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(25);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.SHARE, Permission.DELETE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueTwentyEight_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(28);

        assertThat(permissions).containsOnly(Permission.SHARE, Permission.DELETE, Permission.UPDATE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueThirtyOne_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permissions.buildPermissions(31);

        assertThat(permissions).containsOnly(
            Permission.CREATE, Permission.READ, Permission.UPDATE, Permission.SHARE, Permission.DELETE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueHighValue_ShouldThrowUnsupportedPermission() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permissions.buildPermissions(32))
            .withMessage("The given permissions are not supported");
    }

    @Test
    public void buildPermissions_sumOfPermissionsNegativeValue_ExpectArray() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permissions.buildPermissions(-5))
            .withMessage("The given permissions are not supported");
    }
}
