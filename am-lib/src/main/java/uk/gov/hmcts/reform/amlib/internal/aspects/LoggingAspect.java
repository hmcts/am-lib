package uk.gov.hmcts.reform.amlib.internal.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Slf4j
@SuppressWarnings("LineLenght")
public class LoggingAspect {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^{}]+)}}");

    private final Map<MethodSignature, Metadata> cache = new ConcurrentHashMap<>();

    @AfterReturning(pointcut = "within(uk.gov.hmcts.reform.amlib.*Service) && execution(@AuditLog public * *(..))", returning = "result")
    public void after(JoinPoint joinPoint, Object result) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AuditLog auditLog = methodSignature.getMethod().getAnnotation(AuditLog.class);

        if (isEnabled(auditLog.severity())) {
            Metadata metadata = cache.computeIfAbsent(methodSignature, createMetadata(methodSignature, auditLog));

            String template = auditLog.value();
            for (Metadata.Expression expression : metadata.expressions) {
                Object value;

                Object beanInstance = expression.value.startsWith("result") ? result : joinPoint.getArgs()[expression.argumentPosition];
                if (expression.beanProperties != null) {
                    value = extractValue(beanInstance, expression.beanProperties);
                } else {
                    value = beanInstance;
                }

                template = template.replace(expression.template, Objects.toString(value));
            }
            log(auditLog.severity(), "[Access Management audit]: " + template);
        }
    }

    private Function<MethodSignature, Metadata> createMetadata(MethodSignature methodSignature, AuditLog auditLog) {
        return method -> {
            Matcher matcher = VARIABLE_PATTERN.matcher(auditLog.value());

            Metadata instance = new Metadata();
            while (matcher.find()) {
                Metadata.Expression expression = new Metadata.Expression();
                expression.template = matcher.group(0);
                expression.value = matcher.group(1);

                if (expression.value.contains(".")) {
                    expression.beanName = extractBeanName(expression.value);
                    expression.beanProperties = extractBeanProperties(expression.value);
                } else {
                    expression.beanName = expression.value;
                    expression.beanProperties = null;
                }

                String[] parameterNames = methodSignature.getParameterNames();
                expression.argumentPosition = Arrays.asList(parameterNames).indexOf(expression.beanName);

                instance.expressions.add(expression);
            }
            return instance;
        };
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

    private static class Metadata {
        private List<Expression> expressions = new ArrayList<>();

        private static class Expression {
            private String value;
            private String template;
            private String beanName;
            private String beanProperties;
            private Integer argumentPosition;
        }
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
