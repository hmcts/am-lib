package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
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
                                                                  String accessorIds,
                                                                  Set<Permission> permissions) {
        return createGrant(resourceId, accessorIds, createPermissionsForWholeDocument(permissions));
    }

    public static ExplicitAccessGrant createGrantForWholeDocument(String resourceId,
                                                                  Set<String> accessorIds,
                                                                  Set<Permission> permissions) {
        return createGrant(resourceId, accessorIds, ROLE_NAME, createPermissionsForWholeDocument(permissions));
    }

    public static ExplicitAccessGrant createGrant(String resourceId,
                                                  String accessorIds,
                                                  Map<JsonPointer, Set<Permission>> attributePermissions) {
        return createGrant(resourceId, ImmutableSet.of(accessorIds), ROLE_NAME, attributePermissions);
    }

    public static ExplicitAccessGrant createGrant(String resourceId,
                                                  String accessorIds,
                                                  String relationship,
                                                  Map<JsonPointer, Set<Permission>> attributePermissions) {
        return createGrant(resourceId, ImmutableSet.of(accessorIds), relationship, attributePermissions);
    }

    public static ExplicitAccessGrant createGrant(String resourceId,
                                                  Set<String> accessorIds,
                                                  String relationship,
                                                  Map<JsonPointer, Set<Permission>> attributePermissions) {
        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(accessorIds)
            .accessorType(ACCESSOR_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(attributePermissions)
            .securityClassification(SECURITY_CLASSIFICATION)
            .relationship(relationship)
            .build();
    }

    public static Map<JsonPointer, Set<Permission>> createPermissionsForWholeDocument(Set<Permission> permissions) {
        return createPermissions("", permissions);
    }

    public static Map<JsonPointer, Set<Permission>> createPermissions(String attribute,
                                                                      Set<Permission> permissions) {
        return ImmutableMap.of(JsonPointer.valueOf(attribute), permissions);
    }

    public static ExplicitAccessMetadata createMetadata(String resourceId) {
        return ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessorType(ACCESSOR_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute(JsonPointer.valueOf(""))
            .securityClassification(SecurityClassification.PUBLIC)
            .build();
    }

    public static Resource createResource(String resourceId) {
        return Resource.builder()
            .id(resourceId)
            .definition(ResourceDefinition.builder()
                .resourceName(RESOURCE_NAME)
                .resourceType(RESOURCE_TYPE)
                .serviceName(SERVICE_NAME)
                .build())
            .data(DATA)
            .build();
    }
}
