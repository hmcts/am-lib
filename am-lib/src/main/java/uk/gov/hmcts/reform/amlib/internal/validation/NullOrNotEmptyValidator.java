package uk.gov.hmcts.reform.amlib.internal.validation;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class NullOrNotEmptyValidator implements ConstraintValidator<NullOrNotEmpty,
    Map<JsonPointer, SecurityClassification>> {

    @Override
    public void initialize(NullOrNotEmpty parameters) {
        // Nothing to do here
    }

    @Override
    public boolean isValid(Map<JsonPointer, SecurityClassification> attributeSecurityClassifications,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (attributeSecurityClassifications != null
            && attributeSecurityClassifications.isEmpty()) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                .buildConstraintViolationWithTemplate("attributeSecurityClassifications - must not be empty")
                .addConstraintViolation();
            return false;
        }
        return true;
    }
}
