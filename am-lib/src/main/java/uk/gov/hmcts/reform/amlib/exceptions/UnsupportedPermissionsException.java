package uk.gov.hmcts.reform.amlib.exceptions;

/**
 * Thrown when access management was misconfigured with unknown permissions.
 */
public class UnsupportedPermissionsException extends AccessManagementException {
    private static final long serialVersionUID = 1L;

    public UnsupportedPermissionsException() {
        super("The given permissions are not supported");
    }
}
