package uk.gov.hmcts.reform.amlib.internal.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Validator for attribute security classification, which can be either null or
 * a map of attribute security classifications containing at least the root
 * attribute.</p>
 *
 * <p>Throws an IllegalArgumentException if validation finds an empty map or a map
 * without the root attribute security classification defined.</p>
 *
 * <p>For details of the validator implementation, see
 * {@link AttributeSecurityClassificationValidator}</p>
 *
 */
@Target({ElementType.PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = AttributeSecurityClassificationValidator.class)
public @interface ValidAttributeSecurityClassification {
    String message() default "{javax.validation.constraints.Pattern.message}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default {};
}
