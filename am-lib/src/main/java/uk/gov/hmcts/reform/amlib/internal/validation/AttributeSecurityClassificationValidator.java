package uk.gov.hmcts.reform.amlib.internal.validation;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Map;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AttributeSecurityClassificationValidator implements
    ConstraintValidator<ValidAttributeSecurityClassification, Map<JsonPointer, SecurityClassification>> {

    @Override
    public void initialize(ValidAttributeSecurityClassification parameters) {
        // Nothing to do here
    }

    @Override
    public boolean isValid(Map<JsonPointer, SecurityClassification> attributeSecurityClassifications,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (attributeSecurityClassifications != null) {
            if (attributeSecurityClassifications.isEmpty()) {
                setConstraintValidatorContextMessage("must not be empty",
                    constraintValidatorContext);
                return false;
            } else if (attributeSecurityClassifications.get(JsonPointer.valueOf("")) == null) {
                setConstraintValidatorContextMessage("must contain root attribute",
                    constraintValidatorContext);
                return false;
            }
        }
        return true;
    }

    private void setConstraintValidatorContextMessage(String message,
                                                      ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext
            .buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
