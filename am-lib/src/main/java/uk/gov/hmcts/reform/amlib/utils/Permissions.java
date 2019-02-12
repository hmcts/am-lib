package uk.gov.hmcts.reform.amlib.utils;

import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class Permissions {

    private Permissions() {

    }

    public static int sumOf(Set<Permission> perms) {
        int sum = 0;

        for (Permission permission : perms) {
            sum += permission.getValue();
        }

        return sum;
    }

    /**
     * Builds a list of permissions based on integer value. HIDE is removed from values above 0, as HIDE only
     * permits itself as a lone permission.
     *
     * @param sumOfPermissions the decimal value of permissions defined in Permission enum
     * @return Returns a list of permissions.
     * @throws UnsupportedPermissionsException when sumOfPermissions is negative or larger than 31.
     */
    public static Set<Permission> buildPermissions(int sumOfPermissions) throws UnsupportedPermissionsException {

        if (sumOfPermissions < 0 || sumOfPermissions > 31) {
            throw new UnsupportedPermissionsException();
        }

        return Arrays.stream(Permission.values())
            .filter(permission -> permission.isGranted(sumOfPermissions))
            .collect(Collectors.toSet());
    }
}
