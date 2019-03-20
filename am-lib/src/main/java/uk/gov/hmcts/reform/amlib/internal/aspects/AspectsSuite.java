package uk.gov.hmcts.reform.amlib.internal.aspects;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("ValidationAspect, ErrorHandlingAspect, LoggingAspect")
public class AspectsSuite {
}
