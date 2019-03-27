package uk.gov.hmcts.reform.amlib.internal.aspects;

import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import uk.gov.hmcts.reform.amlib.internal.aspects.AuditingAspect.InvalidTemplateExpressionException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class AuditingAspectTest {
    private final AuditingAspect aspect = new AuditingAspect();

    @AfterEach
    void cleanMessages() {
        StaticLogAppender.clear();
    }

    @Test
    void whenTemplateHasNoPlaceholdersShouldPrintMessageUnchanged() {
        JoinPoint joinPoint = createJoinPoint("access changed");

        aspect.after(joinPoint, null);

        assertThat(StaticLogAppender.getMessage())
            .isEqualTo("[Access Management audit]: access changed");
    }

    @Test
    void whenTemplateHasPlaceholderWithMethodArgumentExpressionShouldPrintMessageWithExtractedValue() {
        JoinPoint joinPoint = createJoinPoint("access to '{{resource}}' changed");
        mockInputArgs(joinPoint, "resource", "ae4c7");

        aspect.after(joinPoint, null);

        assertThat(StaticLogAppender.getMessage())
            .isEqualTo("[Access Management audit]: access to 'ae4c7' changed");
    }

    @Test
    void whenTemplateHasPlaceholderWithMethodResultExpressionShouldPrintMessageWithExtractedValue() {
        JoinPoint joinPoint = createJoinPoint("access changed - new permissions are {{result}}");

        aspect.after(joinPoint, "CRUD");

        assertThat(StaticLogAppender.getMessage())
            .isEqualTo("[Access Management audit]: access changed - new permissions are CRUD");
    }

    @Test
    void whenTemplateHasPlaceholderWithMappedDiagnosticContextExpressionShouldPrintMessageWithExtractedValue() {
        JoinPoint joinPoint = createJoinPoint("access changed by '{{mdc:caller}}'");

        MDC.put("caller", "Administrator");
        aspect.after(joinPoint, null);
        MDC.clear();

        assertThat(StaticLogAppender.getMessage())
            .isEqualTo("[Access Management audit]: access changed by 'Administrator'");
    }

    @Test
    @SuppressWarnings("LineLength")
    void whenTemplateHasAllSupportedPlaceholderExpressionsShouldPrintMessageWithExtractedValues() {
        JoinPoint joinPoint = createJoinPoint("access to '{{resource}}' changed by '{{mdc:caller}}' - new permissions are {{result}}");
        mockInputArgs(joinPoint, "resource", "ae4c7");

        MDC.put("caller", "Administrator");
        aspect.after(joinPoint, "CRUD");
        MDC.clear();

        assertThat(StaticLogAppender.getMessage())
            .isEqualTo("[Access Management audit]: access to 'ae4c7' changed by 'Administrator' - new permissions are CRUD");
    }

    @Nested
    @DisplayName("whenValueIsMissing")
    class WhenValueIsMissing {
        @Test
        void whenTemplateHasPlaceholderWithMethodArgumentExpressionShouldPrintMessageWithNull() {
            JoinPoint joinPoint = createJoinPoint("access to '{{resource}}' changed");
            mockInputArgs(joinPoint, "resource", null);

            aspect.after(joinPoint, null);

            assertThat(StaticLogAppender.getMessage())
                .isEqualTo("[Access Management audit]: access to 'null' changed");
        }

        @Test
        void whenTemplateHasPlaceholderWithMethodResultExpressionShouldPrintMessageWithNull() {
            JoinPoint joinPoint = createJoinPoint("access changed - new permissions are {{result}}");

            aspect.after(joinPoint, null);

            assertThat(StaticLogAppender.getMessage())
                .isEqualTo("[Access Management audit]: access changed - new permissions are null");
        }

        @Test
        void whenTemplateHasPlaceholderWithMappedDiagnosticContextExpressionShouldPrintMessageWithNull() {
            JoinPoint joinPoint = createJoinPoint("access changed by '{{mdc:caller}}'");

            MDC.put("caller", null);
            aspect.after(joinPoint, null);
            MDC.clear();

            assertThat(StaticLogAppender.getMessage())
                .isEqualTo("[Access Management audit]: access changed by 'null'");
        }
    }

    @Nested
    @DisplayName("whenExpressionHasProperties")
    class WhenExpressionHasProperties {
        @Test
        void whenTemplateHasPlaceholderWithMethodArgumentExpressionShouldPrintMessageWithExtractedValue() {
            JoinPoint joinPoint = createJoinPoint("access to '{{resource.id}}' changed");
            mockInputArgs(joinPoint, "resource", new Resource("ae4c7"));

            aspect.after(joinPoint, null);

            assertThat(StaticLogAppender.getMessage())
                .isEqualTo("[Access Management audit]: access to 'ae4c7' changed");
        }

        @AllArgsConstructor
        class Resource {
            private final String id;
        }

        @Test
        void whenTemplateHasPlaceholderWithMethodResultExpressionShouldPrintMessageWithExtractedValue() {
            JoinPoint joinPoint = createJoinPoint("access changed - new permissions are {{result.value}}");

            aspect.after(joinPoint, new Permissions("CRUD"));

            assertThat(StaticLogAppender.getMessage())
                .isEqualTo("[Access Management audit]: access changed - new permissions are CRUD");
        }

        @AllArgsConstructor
        class Permissions {
            private final String value;
        }
    }

    @Nested
    @DisplayName("whenErrors")
    class WhenErrors {
        @Test
        void whenTemplateHasPlaceholderWithMethodArgumentExpressionNotMatchingArgumentNamesShouldThrowException() {
            JoinPoint joinPoint = createJoinPoint("access to '{{r}}' changed");
            mockInputArgs(joinPoint, "resource", "ae4c7");

            assertThatExceptionOfType(InvalidTemplateExpressionException.class)
                .isThrownBy(() -> aspect.after(joinPoint, null))
                .withMessage("Argument 'r' does not exist among method arguments 'resource'");
        }

        @Test
        void whenTemplateHasPlaceholderExpressionWithPropertyNotExistingShouldThrowException() {
            JoinPoint joinPoint = createJoinPoint("access to '{{resource.id}}' changed");
            mockInputArgs(joinPoint, "resource", "ae4c7");

            assertThatExceptionOfType(InvalidTemplateExpressionException.class)
                .isThrownBy(() -> aspect.after(joinPoint, null))
                .withMessage("Cannot find fragment id in expression id against instance of class java.lang.String");
        }
    }

    private JoinPoint createJoinPoint(String template) {
        return createJoinPoint(template, AuditLog.Severity.INFO);
    }

    private JoinPoint createJoinPoint(String template, AuditLog.Severity severity) {
        AuditLog auditLog = mock(AuditLog.class);
        when(auditLog.value()).thenReturn(template);
        when(auditLog.severity()).thenReturn(severity);

        Method method = mock(Method.class);
        when(method.getAnnotation(AuditLog.class)).thenReturn(auditLog);

        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);

        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);

        return joinPoint;
    }

    private void mockInputArgs(JoinPoint joinPoint, String name, Object value) {
        when(((MethodSignature) joinPoint.getSignature()).getParameterNames()).thenReturn(new String[]{name});
        when(joinPoint.getArgs()).thenReturn(new Object[]{value});
    }

}
