package uk.gov.hmcts.reform.amlib.helpers;

import java.util.Set;

public final class ValidationMessageRegexFactory {

    private ValidationMessageRegexFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String validationMessageRegex(Set<String> fields, String rules) {
        return String.format("[^;]+(%s) - must not be (%s)", String.join("|", fields), rules);
    }
}
