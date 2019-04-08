package uk.gov.hmcts.reform.amlib.internal.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.jdbi.v3.core.ConnectionException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErrorHandlingAspectTest {
    private final ErrorHandlingAspect aspect = new ErrorHandlingAspect();

    @Test
    void whenNoExceptionIsThrownShouldPassthroughResult() throws Throwable {
        String result = "Some result";

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn(result);

        assertThat(aspect.around(joinPoint)).isEqualTo(result);
    }

    @Test
    void whenRawDatabaseExceptionIsThrownShouldWrapItInPersistenceException() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenThrow(new ConnectionException(null));

        assertThatExceptionOfType(PersistenceException.class)
            .isThrownBy(() -> aspect.around(joinPoint))
            .withMessage("Operation on persistent store failed. If transaction was used, it has been rolled back. "
                + "Cause: org.jdbi.v3.core.ConnectionException");
    }

}
