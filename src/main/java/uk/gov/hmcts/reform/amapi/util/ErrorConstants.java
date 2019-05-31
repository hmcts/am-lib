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
}
