package uk.gov.hmcts.reform.amlib.internal.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Slf4j
public class LoggingAspect {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^{}]+)}}");

    @Around("within(uk.gov.hmcts.reform.amlib.*Service) && execution(@AuditLog public * *(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (log.isInfoEnabled()) {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String template = methodSignature.getMethod().getAnnotation(AuditLog.class).value();

            Matcher matcher = VARIABLE_PATTERN.matcher(template);
            while (matcher.find()) {
                String expression = matcher.group(1);

                Object arg;
                if (expression.startsWith("result")) {
                    if (expression.contains(".")) {
                        arg = extractValue(result, expression.substring(expression.indexOf('.') + 1));
                    } else {
                        arg = result;
                    }
                } else {
                    String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
                    int index = Arrays.asList(parameterNames).indexOf(
                        expression.contains(".") ? expression.substring(0, expression.indexOf('.')) : expression
                    );

                    if (expression.contains(".")) {
                        arg = extractValue(joinPoint.getArgs()[index], expression.substring(expression.indexOf('.') + 1));
                    } else {
                        arg = joinPoint.getArgs()[index];
                    }
                }

                template = template.replace(matcher.group(0), Objects.toString(arg));
            }

            log.info("[Access Management audit]: " + template);
        }

        return result;
    }

    private Object extractValue(Object object, String path) {
        if (object == null) {
            return null;
        }

        Object result = object;
        for (String fragment : path.split("\\.")) {
            if (result == null) {
                break;
            }
            try {
                Field field = result.getClass().getDeclaredField(fragment);
                field.setAccessible(true);
                result = field.get(result);
            } catch (Exception e) {
                String template = "Cannot find fragment %s in expression %s against instance of %s class";
                throw new InvalidTemplateExpressionException(String.format(template, fragment, path, object.getClass()), e);
            }
        }

        return result;
    }

    private static class InvalidTemplateExpressionException extends AuditException {
        private InvalidTemplateExpressionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class AuditException extends RuntimeException {
        private AuditException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
