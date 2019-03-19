package uk.gov.hmcts.reform.amlib.internal.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.lang.reflect.Method;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.executable.ExecutableValidator;

import static java.util.stream.Collectors.joining;

@Aspect
public class ValidationAspect {

    private final ExecutableValidator executableValidator = Validation.byProvider(HibernateValidator.class)
        .configure()
        .messageInterpolator(new ParameterMessageInterpolator())
        .buildValidatorFactory()
        .getValidator()
        .forExecutables();

    @Around("execution(public * uk.gov.hmcts.reform.amlib.*Service.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("ValidationAspect.around");
        Set<ConstraintViolation<Object>> violations = validate(joinPoint);

        if (!violations.isEmpty()) {
            String messages = violations.stream().map(this::toMessage).collect(joining("; "));
            throw new IllegalArgumentException(messages);
        }

        return joinPoint.proceed();
    }

    private Set<ConstraintViolation<Object>> validate(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        return executableValidator.validateParameters(joinPoint.getTarget(), method, joinPoint.getArgs());
    }

    private String toMessage(ConstraintViolation<Object> violation) {
        return violation.getPropertyPath() + " - " + violation.getMessage();
    }
}
