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
                String[] split = matcher.group(1).split("#", 2);
                String source = split[0];
                String variable = split.length == 2 ? split[1] : null;

                Object arg;
                switch (source) {
                    case "in":
                        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
                        int index = Arrays.asList(parameterNames).indexOf(
                            variable.contains(".") ? variable.substring(0, variable.indexOf('.')) : variable
                        );

                        if (variable.contains(".")) {
                            arg = extractValue(joinPoint.getArgs()[index], variable.substring(variable.indexOf('.') + 1));
                        } else {
                            arg = joinPoint.getArgs()[index];
                        }
                        break;
                    case "out":
                        if (variable == null) {
                            arg = result;
                        } else {
                            arg = extractValue(result, variable);
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown source");
                }

                template = template.replace(matcher.group(0), Objects.toString(arg));
            }

            log.info("[AccessManagement audit]: " + template);
        }

        return result;
    }

    private Object extractValue(Object object, String path) {
        String[] fragments = path.split("\\.");

        Object result = object;
        for (String fragment : fragments) {
            try {
                Field field = result.getClass().getDeclaredField(fragment);
                field.setAccessible(true);
                result = field.get(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

}
