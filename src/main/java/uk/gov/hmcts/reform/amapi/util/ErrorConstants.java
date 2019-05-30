package uk.gov.hmcts.reform.amapi.util;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings({
    "checkstyle:HideUtilityClassConstructor",
    "PMD.FieldNamingConventions"
})
public class ErrorConstants {

    public static String MALFORMED_JSON =  "Malformed Input Request";

    public static String RESOURCE_NOT_FOUND =  "Resource Not Found";

    public static String UNSUPPORTED_MEDIA_TYPES = "Unsupported Media Types";
}
