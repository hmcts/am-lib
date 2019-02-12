package uk.gov.hmcts.reform.amlib.exceptions;

public class UnsupportedPermissionsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnsupportedPermissionsException() {
        super("The given permissions are not supported");
    }
}
