package uk.gov.hmcts.reform.amlib.internal.utils;

import uk.gov.hmcts.reform.amlib.exceptions.AccessManagementException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SuppressWarnings({"checkstyle:HideUtilityClassConstructor"})
public class PropertyReader {

    private static Properties properties;

    public static final String AUDIT_REQUIRED = "audit.required";

    private static void readProperties() {
        properties = new Properties();
        final InputStream stream = PropertyReader.class
            .getResourceAsStream("/audit.properties");
        try {
            properties.load(stream);
        } catch (final IOException ex) {
            throw new AccessManagementException("Error in Reading Audit properties", ex);
        }
    }

    /**
     * Reading Properties.
     *
     * @param name Name
     * @return propertyValue
     */
    public static String getPropertyValue(String name) {
        readProperties();
        return properties.getProperty(name);
    }
}
