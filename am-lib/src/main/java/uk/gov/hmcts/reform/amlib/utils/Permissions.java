package uk.gov.hmcts.reform.amlib.utils;

import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class Permissions {

    private Permissions() {

    }

    /**
     * When saving a record into Access Management all the values are summed up (the 'sumOf' method) and saved as int.
     * @param perms a set of permission enum values e.g. ("CREATE", "READ") to be converted to integer value.
     * @return the sum of permissions.
     */

    public static int sumOf(Set<Permission> perms) {
        int sum = 0;

        for (Permission permission : perms) {
            sum += permission.getValue();
        }

        return sum;
    }

    /**
     * Builds a list of permissions based on integer value.
     *
     * @param sumOfPermissions the decimal value of permissions defined in Permission enum
     * @return a list of permissions.
     * @throws UnsupportedPermissionsException when sumOfPermissions is negative or larger than 31.
     */
    public static Set<Permission> fromSumOf(int sumOfPermissions) throws UnsupportedPermissionsException {

        if (sumOfPermissions < 0 || sumOfPermissions > 31) {
            throw new UnsupportedPermissionsException();
        }

        return Arrays.stream(Permission.values())
            .filter(permission -> permission.isGranted(sumOfPermissions))
            .collect(Collectors.toSet());
    }
}
