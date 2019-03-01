package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

public final class TestConstants {

    public static final String ACCESS_TYPE = "user";
    public static final String SERVICE_NAME = "Service";
    public static final String RESOURCE_TYPE = "Resource Type";
    public static final String RESOURCE_NAME = "resource";
    public static final String SECURITY_CLASSIFICATION = "PUBLIC";
    public static final String ATTRIBUTE = "/test";
    public static final String ACCESSOR_ID = "a";
    public static final String OTHER_ACCESSOR_ID = "b";
    public static final String ROLE_NAME = "Role Name";
    public static final Set<Permission> EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS =
        Stream.of(CREATE, READ, UPDATE).collect(toSet());
    public static final Set<Permission> CREATE_PERMISSION = Stream.of(CREATE).collect(toSet());
    public static final Set<Permission> READ_PERMISSION = Stream.of(READ).collect(toSet());
    public static final JsonNode DATA = JsonNodeFactory.instance.objectNode();

    private TestConstants() {
        //NO-OP
    }
}
