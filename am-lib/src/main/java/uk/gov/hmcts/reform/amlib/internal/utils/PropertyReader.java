package uk.gov.hmcts.reform.amlib.internal.utils;

import java.util.ResourceBundle;

public final class PropertyReader {

    public static final String AUDIT_REQUIRED = "audit.required";

    private static ResourceBundle rb = ResourceBundle.getBundle("audit");

    private PropertyReader() {
        super();
    }

    /**
     * Reading Properties.
     *
     * @param name Name
     * @return propertyValue
     */
    public static String getPropertyValue(String name) {
        return rb.getString(name);
    }
}
