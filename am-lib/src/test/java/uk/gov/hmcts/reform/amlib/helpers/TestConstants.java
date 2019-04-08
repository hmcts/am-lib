package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Set;

import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

public final class TestConstants {

    public static final String SERVICE_NAME = "Family Public Law";
    public static final String RESOURCE_TYPE = "Case";
    public static final String RESOURCE_NAME = "Supervision, EPO care order";
    public static final JsonPointer ROOT_ATTRIBUTE = JsonPointer.valueOf("");
    public static final String ATTRIBUTE = "/parent";
    public static final String CHILD_ATTRIBUTE = "/parent/child";
    public static final String ACCESSOR_ID = "dc84d6b1-70f9-4991-836c-ee30fd1c3914";
    public static final Set<String> ROLE_NAMES = ImmutableSet.of("Solicitor");
    public static final Set<String> ACCESSOR_IDS = ImmutableSet.of(
        "816127cc-e68e-47ab-b57d-819eca5f0362",
        "57557976-0683-43bc-a8fb-6817beaf21f4");
    public static final String ROLE_NAME = "Solicitor";
    public static final String OTHER_ROLE_NAME = "Local Authority";
    public static final Set<Permission> EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS = ImmutableSet.of(CREATE, READ, UPDATE);
    public static final Set<Permission> CREATE_PERMISSION = ImmutableSet.of(CREATE);
    public static final Set<Permission> READ_PERMISSION = ImmutableSet.of(READ);
    public static final JsonNode DATA = JsonNodeFactory.instance.objectNode()
        .put("name", "John")
        .put("age", 18);

    private TestConstants() {
        //NO-OP
    }
}
