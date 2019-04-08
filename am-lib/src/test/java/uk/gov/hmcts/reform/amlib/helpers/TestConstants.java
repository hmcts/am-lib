package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Set;

import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;

public final class TestConstants {

    public static final AccessorType ACCESSOR_TYPE = AccessorType.USER;
    public static final AccessManagementType ACCESS_MANAGEMENT_TYPE = AccessManagementType.EXPLICIT;
    public static final String SERVICE_NAME = "Service";
    public static final String RESOURCE_TYPE = "Resource Type";
    public static final String RESOURCE_NAME = "resource";
    public static final SecurityClassification SECURITY_CLASSIFICATION = SecurityClassification.PUBLIC;
    public static final JsonPointer ROOT_ATTRIBUTE = JsonPointer.valueOf("");
    public static final JsonPointer ATTRIBUTE = JsonPointer.valueOf("/test");
    public static final String ACCESSOR_ID = "a";
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
    public static final ResourceDefinition RESOURCE_DEFINITION = ResourceDefinition.builder()
        .serviceName(SERVICE_NAME)
        .resourceType(RESOURCE_TYPE)
        .resourceName(RESOURCE_NAME)
        .build();

    private TestConstants() {
        //NO-OP
    }
}
