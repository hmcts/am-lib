package uk.gov.hmcts.reform.amlib.exceptions;

public class ErrorAddingEntriesToDatabaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ErrorAddingEntriesToDatabaseException() {
        super("There was an error adding a row into the database. All rows have been rolled back.");
    }
}
