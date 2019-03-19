package uk.gov.hmcts.reform.amlib.exceptions;

/**
 * Exception thrown when operation requiring persistent data store has failed,
 * usually wrapping exceptions from persistence framework of choice.
 */
public class PersistenceException extends AccessManagementException {
    private static final long serialVersionUID = 1L;

    public PersistenceException(Throwable ex) {
        super("Operation on persistent store failed. In case operation run in transaction it has been rolled back. "
            + "Cause: " + ex, ex);
    }
}
