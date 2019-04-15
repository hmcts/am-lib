package uk.gov.hmcts.reform.amlib.utils;

import static java.lang.System.getenv;

public class EnvironmentVariableUtils {
    private EnvironmentVariableUtils() {
        throw new UnsupportedOperationException("Constructing utility class is not supported");
    }

    public static String getValueOrDefault(String name, String defaultValue) {
        String value = getenv(name);
        return value != null ? value : defaultValue;
    }

    public static String getValueOrThrow(String name) {
        String value = getenv(name);
        if (value == null) {
            throw new IllegalArgumentException("Environment variable '" + name + "' is missing");
        }
        return value;
    }
}
