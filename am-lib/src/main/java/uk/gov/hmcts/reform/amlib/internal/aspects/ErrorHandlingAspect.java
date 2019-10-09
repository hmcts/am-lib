package uk.gov.hmcts.reform.amlib.internal.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jdbi.v3.core.JdbiException;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;

@Aspect
public class ErrorHandlingAspect {

    @Around("execution(public * uk.gov.hmcts.reform.amlib.*Service*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (JdbiException ex) {
            throw new PersistenceException(ex);
        }
    }
}
