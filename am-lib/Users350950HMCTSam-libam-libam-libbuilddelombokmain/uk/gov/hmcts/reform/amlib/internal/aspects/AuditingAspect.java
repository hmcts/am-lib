package uk.gov.hmcts.reform.amlib.internal.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
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
import static java.lang.String.format;
import static java.lang.String.join;

@Aspect
public class AuditingAspect {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuditingAspect.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^{}]+)}}");
    private final Map<MethodSignature, Metadata> cache = new ConcurrentHashMap<>();

    @AfterReturning(pointcut = "execution(@AuditLog public * uk.gov.hmcts.reform.amlib.*Service.*(..))", returning = "result")
    public void after(JoinPoint joinPoint, Object result) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        AuditLog auditLog = methodSignature.getMethod().getAnnotation(AuditLog.class);
        if (isEnabled(auditLog.severity())) {
            Metadata metadata = cache.computeIfAbsent(methodSignature, createMetadata(auditLog.value(), methodSignature.getParameterNames()));
            String template = auditLog.value();
            for (Metadata.Expression expression : metadata.expressions) {
                Object value;
                Object beanInstance;
                if (Keyword.MDC.matches(expression.beanName)) {
                    beanInstance = MDC.get(expression.beanName.substring(expression.beanName.indexOf(':') + 1));
                } else if (Keyword.RESULT.matches(expression.beanName)) {
                    beanInstance = result;
                } else {
                    beanInstance = joinPoint.getArgs()[expression.argumentPosition];
                }
                if (expression.beanProperties == null) {
                    value = beanInstance;
                } else {
                    value = extractValue(beanInstance, expression.beanProperties);
                }
                template = template.replace(expression.template, Objects.toString(value));
            }
            log(auditLog.severity(), "[Access Management audit]: " + template);
        }
    }

    // New objects need to be created in while loop
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Function<MethodSignature, Metadata> createMetadata(String template, String... parameterNames) {
        return method -> {
            Matcher matcher = VARIABLE_PATTERN.matcher(template);
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
                }
                if (!Keyword.MDC.matches(expression.beanName) && !Keyword.RESULT.matches(expression.beanName)) {
                    expression.argumentPosition = Arrays.asList(parameterNames).indexOf(expression.beanName);
                    if (expression.argumentPosition < 0) {
                        String msgTemplate = "Argument \'%s\' does not exist among method arguments \'%s\'";
                        throw new InvalidTemplateExpressionException(format(msgTemplate, expression.beanName, join(", ", parameterNames)));
                    }
                }
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
     * Removes bean name from expression formatted as {@code <bean name>[.<property name>]+} leaving bean properties.
     */
    private String extractBeanProperties(String expression) {
        return expression.substring(expression.indexOf('.') + 1);
    }

    /**
     * Removes property names from expression formatted as {@code <bean name>[.<property name>]+} leaving bean name.
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
                String msgTemplate = "Cannot find fragment %s in expression %s against instance of %s";
                throw new InvalidTemplateExpressionException(format(msgTemplate, fragment, path, object.getClass()), e);
            }
        }
        return result;
    }


    private enum Keyword {
        MDC("mdc:"), RESULT("result");
        private String prefix;

        Keyword(String prefix) {
            this.prefix = prefix;
        }

        boolean matches(String value) {
            return value.startsWith(prefix);
        }
    }


    private static class Metadata {
        private final List<Expression> expressions = new ArrayList<>();


        private static class Expression {
            private String value;
            private String template;
            private String beanName;
            private String beanProperties;
            private Integer argumentPosition;

            @Override
            public boolean equals(final Object o) {
                if (o == this) return true;
                if (!(o instanceof AuditingAspect.Metadata.Expression)) return false;
                final AuditingAspect.Metadata.Expression other = (AuditingAspect.Metadata.Expression) o;
                if (!other.canEqual((Object) this)) return false;
                final Object this$value = this.value;
                final Object other$value = other.value;
                if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
                final Object this$template = this.template;
                final Object other$template = other.template;
                if (this$template == null ? other$template != null : !this$template.equals(other$template)) return false;
                final Object this$beanName = this.beanName;
                final Object other$beanName = other.beanName;
                if (this$beanName == null ? other$beanName != null : !this$beanName.equals(other$beanName)) return false;
                final Object this$beanProperties = this.beanProperties;
                final Object other$beanProperties = other.beanProperties;
                if (this$beanProperties == null ? other$beanProperties != null : !this$beanProperties.equals(other$beanProperties)) return false;
                final Object this$argumentPosition = this.argumentPosition;
                final Object other$argumentPosition = other.argumentPosition;
                if (this$argumentPosition == null ? other$argumentPosition != null : !this$argumentPosition.equals(other$argumentPosition)) return false;
                return true;
            }

            protected boolean canEqual(final Object other) {
                return other instanceof AuditingAspect.Metadata.Expression;
            }

            @Override
            public int hashCode() {
                final int PRIME = 59;
                int result = 1;
                final Object $value = this.value;
                result = result * PRIME + ($value == null ? 43 : $value.hashCode());
                final Object $template = this.template;
                result = result * PRIME + ($template == null ? 43 : $template.hashCode());
                final Object $beanName = this.beanName;
                result = result * PRIME + ($beanName == null ? 43 : $beanName.hashCode());
                final Object $beanProperties = this.beanProperties;
                result = result * PRIME + ($beanProperties == null ? 43 : $beanProperties.hashCode());
                final Object $argumentPosition = this.argumentPosition;
                result = result * PRIME + ($argumentPosition == null ? 43 : $argumentPosition.hashCode());
                return result;
            }

            @Override
            public String toString() {
                return "AuditingAspect.Metadata.Expression(value=" + this.value + ", template=" + this.template + ", beanName=" + this.beanName + ", beanProperties=" + this.beanProperties + ", argumentPosition=" + this.argumentPosition + ")";
            }
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof AuditingAspect.Metadata)) return false;
            final AuditingAspect.Metadata other = (AuditingAspect.Metadata) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$expressions = this.expressions;
            final Object other$expressions = other.expressions;
            if (this$expressions == null ? other$expressions != null : !this$expressions.equals(other$expressions)) return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof AuditingAspect.Metadata;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $expressions = this.expressions;
            result = result * PRIME + ($expressions == null ? 43 : $expressions.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "AuditingAspect.Metadata(expressions=" + this.expressions + ")";
        }
    }


    static class InvalidTemplateExpressionException extends AuditException {
        private static final long serialVersionUID = 1L;

        private InvalidTemplateExpressionException(String message) {
            super(message);
        }

        private InvalidTemplateExpressionException(String message, Throwable cause) {
            super(message, cause);
        }
    }


    static class AuditException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private AuditException(String message) {
            super(message);
        }

        private AuditException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
