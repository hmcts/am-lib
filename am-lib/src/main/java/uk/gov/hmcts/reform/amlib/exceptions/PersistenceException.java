package uk.gov.hmcts.reform.amlib.exceptions;

public class PersistenceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PersistenceException(Throwable throwable) {
        super("The transaction has been rolled back. Cause: " + throwable.getCause());
    }
}
