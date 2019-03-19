package uk.gov.hmcts.reform.amlib.exceptions;

/**
 * Base unchecked exception for exceptions thrown from Access Management library.
 */
public abstract class AccessManagementException extends RuntimeException {
    public AccessManagementException(String message) {
        super(message);
    }

    public AccessManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
