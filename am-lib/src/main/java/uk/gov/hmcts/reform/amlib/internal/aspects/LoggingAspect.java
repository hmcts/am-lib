package uk.gov.hmcts.reform.amlib.internal.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
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

    @AfterReturning(pointcut = "within(uk.gov.hmcts.reform.amlib.*Service) && execution(@AuditLog public * *(..))", returning = "result")
    public void after(JoinPoint joinPoint, Object result) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AuditLog auditLog = methodSignature.getMethod().getAnnotation(AuditLog.class);

        if (isEnabled(auditLog.severity())) {
            String template = auditLog.value();

            Matcher matcher = VARIABLE_PATTERN.matcher(template);
            while (matcher.find()) {
                String expression = matcher.group(1);

                Object arg;
                if (expression.startsWith("result")) {
                    if (expression.contains(".")) {
                        arg = extractValue(result, extractBeanProperties(expression));
                    } else {
                        arg = result;
                    }
                } else {
                    String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
                    int index = Arrays.asList(parameterNames).indexOf(
                        expression.contains(".") ? extractBeanName(expression) : expression
                    );

                    if (expression.contains(".")) {
                        arg = extractValue(joinPoint.getArgs()[index], extractBeanProperties(expression));
                    } else {
                        arg = joinPoint.getArgs()[index];
                    }
                }

                template = template.replace(matcher.group(0), Objects.toString(arg));
            }

            log(auditLog.severity(), "[Access Management audit]: " + template);
        }
    }

    private boolean isEnabled(AuditLog.Severity severity) {
        switch (severity) {
            case DEBUG:
                return log.isDebugEnabled();
            case INFO:
                return log.isInfoEnabled();
            default:
                throw new AuditException("Unsupported severity: " + severity);
        }
    }

    private void log(AuditLog.Severity severity, String msg) {
        switch (severity) {
            case DEBUG:
                log.debug(msg);
                break;
            case INFO:
                log.info(msg);
                break;
            default:
                throw new AuditException("Unsupported severity: " + severity);
        }
    }

    /**
     * Removes bean name from expression formatted as <bean name>[.<property name>]+ leaving bean properties.
     */
    private String extractBeanProperties(String expression) {
        return expression.substring(expression.indexOf('.') + 1);
    }

    /**
     * Removes property names from expression formatted as <bean name>[.<property name>]+ leaving bean name.
     */
    private String extractBeanName(String expression) {
        return expression.substring(0, expression.indexOf('.'));
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
        private AuditException(String message) {
            super(message);
        }

        private AuditException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
