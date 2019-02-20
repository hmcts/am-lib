package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESS_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SECURITY_CLASSIFICATION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

public final class TestDataFactory {

    private TestDataFactory() {
        //NO-OP
    }

    public static ExplicitAccessGrant grantAccess(String resourceId,
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

    public static ExplicitAccessGrant grantAccessForWholeDocument(String resourceId,
                                                                  Set<Permission> permissions) {
        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(createPermissions("", permissions))
            .securityClassification(SECURITY_CLASSIFICATION)
            .build();
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
            .attribute("")
            .build();
    }
}
