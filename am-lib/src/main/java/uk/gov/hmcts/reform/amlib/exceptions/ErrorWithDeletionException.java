package uk.gov.hmcts.reform.amlib.exceptions;

public class ErrorWithDeletionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ErrorWithDeletionException(Throwable throwable) {
        super("There was an error with removal. All rows have been rolled back. Cause: "
            + throwable.getCause());
    }
}
