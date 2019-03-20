package uk.gov.hmcts.reform.amlib.internal.aspects;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({METHOD})
@Retention(RUNTIME)
public @interface AuditLog {
    String value();

    Severity severity() default Severity.INFO;

    enum Severity {
        DEBUG,
        INFO
    }
}
