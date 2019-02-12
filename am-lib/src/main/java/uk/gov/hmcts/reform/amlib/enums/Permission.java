package uk.gov.hmcts.reform.amlib.enums;

import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exposes a set of enum values used to set permissions for Access Management.
 * Each of the values is a power of two. The reason for that is that in Access Management
 * there might be multiple permissions that need to be assigned: ie. READ + CREATE + HIDE.
 * For convenience when saving a record into AM all the values are summed up (the 'sumOf' method) and saved as integer.
 * In order to determine which individual permissions a record has
 * the binary 'AND' operation is done (the 'hasPermissionTo' method).
 */
public enum Permission {
    HIDE(0),
    CREATE(1),
    READ(2),
    UPDATE(4),
    SHARE(8),
    DELETE(16);

    private int value;

    Permission(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static int sumOf(Set<Permission> perms) {
        int sum = 0;

        for (Permission permission : perms) {
            sum += permission.getValue();
        }

        return sum;
    }

    /**
     * Performs a binary AND operation to determine weather permission can be derived from the sum of permissions.
     *
     * @param permissions       the numeric sum of permissions
     * @return true if particular permission is included is sum of permissions, otherwise false
     */
    public boolean isGranted(int permissions) {
        return (permissions & this.getValue()) == this.getValue();
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

        if (sumOfPermissions > 0) {
            return Arrays.stream(Permission.values())
                .filter(permission -> !HIDE.equals(permission) && permission.isGranted(sumOfPermissions))
                .collect(Collectors.toSet());
        }

        return Arrays.stream(Permission.values())
            .filter(permission -> permission.isGranted(sumOfPermissions))
            .collect(Collectors.toSet());
    }
}
