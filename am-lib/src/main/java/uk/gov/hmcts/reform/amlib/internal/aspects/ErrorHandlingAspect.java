package uk.gov.hmcts.reform.amlib.internal.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jdbi.v3.core.JdbiException;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;

@Aspect
@Slf4j
@SuppressWarnings({"PMD.AvoidPrintStackTrace"})
public class ErrorHandlingAspect {

    @Around("execution(public * uk.gov.hmcts.reform.amlib.*Service.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (JdbiException ex) {
            log.info("JdbiException Exception in aspect:::" + ex.toString());
            log.info("JdbiException Exception cause in aspect:::" + ex.getCause());
            log.info("JdbiException Exception message in aspect:::" + ex.getLocalizedMessage());
            log.error("JdbiException Exception message in aspect:::", ex);
            throw new PersistenceException(ex);
        }
    }
}
