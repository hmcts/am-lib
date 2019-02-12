package uk.gov.hmcts.reform.amlib;

import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class PermissionTest {

    @Test
    public void sumOf_whenPassingPermissions_theSumOfValuesIsCalculated() {
        Set<Permission> permissions = new HashSet<>(Arrays.asList(Permission.CREATE, Permission.READ));

        int sum = Permission.sumOf(permissions);

        int expectedSum = Permission.CREATE.getValue() + Permission.READ.getValue();
        assertThat(sum).isEqualTo(expectedSum);
    }

    @Test
    public void sumOf_whenPassingNoPermissions_theSumOfValuesIsZero() {
        Set<Permission> permissions = new HashSet<>();

        int sum = Permission.sumOf(permissions);

        assertThat(sum).isEqualTo(0);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueZero_OnlyHideIsInArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permission.buildPermissions(0);

        assertThat(permissions).containsOnly(Permission.HIDE);
    }

    @Test

    public void buildPermissions_sumOfPermissionsValueOne_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permission.buildPermissions(1);

        assertThat(permissions).containsOnly(Permission.CREATE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueThree_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permission.buildPermissions(3);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueSeven_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permission.buildPermissions(7);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ, Permission.UPDATE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueThirteen_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permission.buildPermissions(13);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.UPDATE, Permission.SHARE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueThirtyOne_ExpectArray() throws UnsupportedPermissionsException {
        Set<Permission> permissions = Permission.buildPermissions(31);

        assertThat(permissions).containsOnly(
            Permission.CREATE, Permission.READ, Permission.UPDATE, Permission.SHARE, Permission.DELETE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueHighValue_ShouldThrowUnsupportedPermission() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permission.buildPermissions(32))
            .withMessage("The given permissions are not supported");
    }

    @Test
    public void buildPermissions_sumOfPermissionsNegativeValue_ExpectArray() {
        assertThatExceptionOfType(UnsupportedPermissionsException.class).isThrownBy(
            () -> Permission.buildPermissions(-5))
            .withMessage("The given permissions are not supported");
    }
}
