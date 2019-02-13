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

//We do not want this class to be instantiated
public final class Permissions {

    public static final int MAX_PERMISSIONS_VALUE =
        Permissions.sumOf(Stream.of(CREATE, READ, UPDATE, DELETE).collect(Collectors.toSet()));
    public static final int MIN_PERMISSIONS_VALUE = Permissions.sumOf(Stream.of(CREATE).collect(Collectors.toSet()));


    private Permissions() {
        //NO-OP
    }

    /**
     * When saving a record into Access Management all the values are summed up (the 'sumOf' method) and saved as int.
     *
     * @param perms a set of permission enum values e.g. ("CREATE", "READ") to be converted to integer value.
     * @return the sum of permissions.
     */

    public static int sumOf(Set<Permission> perms) {

        return perms.stream().mapToInt(Permission::getValue).sum();
    }

    /**
     * Builds a list of permissions based on integer value.
     *
     * @param sumOfPermissions the decimal value of permissions defined in Permission enum
     * @return a list of permissions.
     * @throws UnsupportedPermissionsException when sumOfPermissions is negative or larger than 31.
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
