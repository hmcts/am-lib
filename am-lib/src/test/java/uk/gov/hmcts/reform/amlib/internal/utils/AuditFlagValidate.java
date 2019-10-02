package uk.gov.hmcts.reform.amlib.internal.utils;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static uk.gov.hmcts.reform.amlib.internal.utils.PropertyReader.AUDIT_REQUIRED;

public class AuditFlagValidate implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED =
        ConditionEvaluationResult.enabled("audit enabled");

    private static final ConditionEvaluationResult DISABLED =
        ConditionEvaluationResult.disabled("audit disabled");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<AnnotatedElement> element = context.getElement();
        Optional<AuditEnabled> auditEnabled = findAnnotation(element, AuditEnabled.class);

        if (auditEnabled.isPresent()) {
            if (auditEnabled.get().value().equalsIgnoreCase(
                PropertyReader.getPropertyValue(AUDIT_REQUIRED))) {
                return ENABLED;
            } else {
                return DISABLED;
            }
        }
        return DISABLED;
    }
}
