package uk.gov.hmcts.reform.amlib.exceptions;

public class ErrorAddingEntriesException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ErrorAddingEntriesException(Throwable throwable) {
        super("There was an error adding a row into the database. All rows have been rolled back. Cause: "
            + throwable.getCause());
    }
}
