package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESS_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SECURITY_CLASSIFICATION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

public final class TestDataFactory {

    private TestDataFactory() {
        //NO-OP
    }

    public static ExplicitAccessGrant createGrantForWholeDocument(String resourceId,
                                                                  Set<Permission> permissions) {
        return createGrantForWholeDocument(resourceId, ACCESSOR_ID, permissions);
    }

    public static ExplicitAccessGrant createGrantForWholeDocument(String resourceId,
                                                                  String accessorId,
                                                                  Set<Permission> permissions) {
        return createGrant(resourceId, accessorId, createPermissionsForWholeDocument(permissions));
    }

    public static ExplicitAccessGrant createGrant(String resourceId,
                                                  String accessorId,
                                                  Map<JsonPointer, Set<Permission>> attributePermissions) {
        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(attributePermissions)
            .securityClassification(SECURITY_CLASSIFICATION)
            .build();
    }

    public static Map<JsonPointer, Set<Permission>> createPermissionsForWholeDocument(Set<Permission> permissions) {
        return createPermissions("", permissions);
    }

    public static Map<JsonPointer, Set<Permission>> createPermissions(String attribute,
                                                                      Set<Permission> permissions) {
        Map<JsonPointer, Set<Permission>> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(attribute), permissions);
        return attributePermissions;
    }

    public static ExplicitAccessMetadata createMetadata(String resourceId) {
        return ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute(JsonPointer.valueOf(""))
            .securityClassification(SecurityClassification.PUBLIC)
            .build();
    }

    public static Resource createResource(String resourceId) {
        return Resource.builder()
            .resourceId(resourceId)
            .type(ResourceDefinition.builder()
                .resourceName(RESOURCE_NAME)
                .resourceType(RESOURCE_TYPE)
                .serviceName(SERVICE_NAME)
                .build())
            .resourceJson(DATA)
            .build();
    }
}
