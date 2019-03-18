package uk.gov.hmcts.reform.amlib.helpers;

public final class ValidationMessageRegexFactory {

    private ValidationMessageRegexFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String expectedValidationMessagesRegex(String... expectedViolations) {
        return String.format("[^;]+(%s)", String.join("|", expectedViolations));
    }
}
