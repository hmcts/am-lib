package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.Set;

import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

public final class TestConstants {

    public static final AccessType ACCESSOR_TYPE = AccessType.USER;
    public static final AccessManagementType ACCESS_MANAGEMENT_TYPE = AccessManagementType.EXPLICIT;
    public static final String SERVICE_NAME = "Service";
    public static final String RESOURCE_TYPE = "Resource Type";
    public static final String RESOURCE_NAME = "resource";
    public static final RoleType ROLE_TYPE = RoleType.IDAM;
    public static final SecurityClassification SECURITY_CLASSIFICATION = SecurityClassification.PUBLIC;
    public static final JsonPointer ROOT_ATTRIBUTE = JsonPointer.valueOf("");
    public static final JsonPointer ATTRIBUTE = JsonPointer.valueOf("/test");
    public static final String PARENT_ATTRIBUTE = "/Parent";
    public static final String CHILD_ATTRIBUTE = "/Parent/child";
    public static final String ACCESSOR_ID = "a";
    public static final String RELATIONSHIP = "Solicitor";
    public static final String OTHER_RELATIONSHIP = "Defendant";
    public static final Set<String> ROLE_NAMES = ImmutableSet.of("Solicitor");
    public static final Set<String> ACCESSOR_IDS = ImmutableSet.of("y", "z");
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
