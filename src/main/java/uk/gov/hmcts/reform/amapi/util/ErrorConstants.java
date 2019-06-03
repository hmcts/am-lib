package uk.gov.hmcts.reform.amapi.util;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings({
    "checkstyle:HideUtilityClassConstructor",
    "PMD.FieldNamingConventions"
})
public class ErrorConstants {

    public static final String MALFORMED_JSON =  "Malformed Input Request";

    public static final String RESOURCE_NOT_FOUND =  "Resource Not Found";

    public static final String UNSUPPORTED_MEDIA_TYPES = "Unsupported Media Types";

    public static final String INVALID_REQUEST =  "There is a problem with your request. Please check and try again";

    public static final String SERVICE_FAILED = "The execution of the service failed";
}
