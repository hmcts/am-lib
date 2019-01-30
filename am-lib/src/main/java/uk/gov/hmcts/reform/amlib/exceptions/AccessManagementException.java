package uk.gov.hmcts.reform.amlib.exceptions;

public class AccessManagementException extends Exception {
    private static final long serialVersionUID = 1L;

    public AccessManagementException() {
        super();
    }

    public AccessManagementException(String message) {
        super(message);
    }
}
