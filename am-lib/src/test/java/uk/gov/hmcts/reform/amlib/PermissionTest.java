package uk.gov.hmcts.reform.amlib;

import org.junit.Test;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public void buildPermissions_sumOfPermissionsValueEqualsZero_returns_OnlyHideInArray() {
        Set<Permission> permissions = Permission.buildPermissions(0);

        assertThat(permissions).containsOnly(Permission.HIDE);
    }

    @Test
    public void buildPermissions_sumOfPermissionsValueNotZero_ExpectArray() {
        Set<Permission> permissions = Permission.buildPermissions(7);

        assertThat(permissions).containsOnly(Permission.CREATE, Permission.READ, Permission.UPDATE);
    }
}
