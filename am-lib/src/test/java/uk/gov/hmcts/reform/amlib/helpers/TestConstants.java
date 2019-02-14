package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import uk.gov.hmcts.reform.amlib.enums.Permissions;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permissions.UPDATE;

public final class TestConstants {
    public static final String ACCESS_TYPE = "user";
    public static final String SERVICE_NAME = "Service 1";
    public static final String RESOURCE_TYPE = "Resource Type 1";
    public static final String RESOURCE_NAME = "resource";
    public static final String SECURITY_CLASSIFICATION = "Public";
    public static final String ACCESSOR_ID = "a";
    public static final String OTHER_ACCESSOR_ID = "b";
    public static final Set<Permissions> EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS =
        Stream.of(CREATE, READ, UPDATE).collect(toSet());
    public static final JsonNode DATA = JsonNodeFactory.instance.objectNode();

    private TestConstants() {
        //NO-OP
    }


}
