package uk.gov.hmcts.reform.amlib.utils;

import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.exceptions.UnsupportedPermissionsException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.DELETE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

public final class Permissions {

    public static final int MIN_PERMISSIONS_VALUE = Permissions.sumOf(CREATE);
    public static final int MAX_PERMISSIONS_VALUE = Permissions.sumOf(CREATE, READ, UPDATE, DELETE);

    private Permissions() {
        //NO-OP
    }

    /**
     * Converts permissions passed in to an integer values and sums them up.
     *
     * @param permissions permission enum values e.g. ("CREATE", "READ") to be converted to integer value
     * @return the sum of permissions
     *
     * @see Permissions#sumOf(Set)
     */
    public static int sumOf(Permission... permissions) {
        return sumOf(Stream.of(permissions).collect(Collectors.toSet()));
    }

    /**
     * Converts permissions passed in to an integer values and sums them up.
     *
     * @param permissions a set of permission enum values e.g. ("CREATE", "READ") to be converted to integer value
     * @return the sum of permissions
     *
     * @see Permissions#sumOf(Permission...)
     */
    public static int sumOf(Set<Permission> permissions) {
        return permissions.stream().mapToInt(Permission::getValue).sum();
    }

    /**
     * Builds a set of permissions based on integer value that represents sum of permissions.
     *
     * @param sumOfPermissions sum of permission integer values defined in {@link Permission} enum
     * @return a list of permissions.
     * @throws UnsupportedPermissionsException when sumOfPermissions is smaller than
     * {@link Permissions#MIN_PERMISSIONS_VALUE} or larger than {@link Permissions#MAX_PERMISSIONS_VALUE}
     */
    public static Set<Permission> fromSumOf(int sumOfPermissions) {
        if (sumOfPermissions < MIN_PERMISSIONS_VALUE || sumOfPermissions > MAX_PERMISSIONS_VALUE) {
            throw new UnsupportedPermissionsException();
        }

        return Arrays.stream(Permission.values())
            .filter(permission -> permission.isGranted(sumOfPermissions))
            .collect(Collectors.toSet());
    }
}
