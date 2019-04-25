package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;

public final class TestDataFactory {

    private TestDataFactory() {
        //NO-OP
    }

    public static ExplicitAccessGrant createGrantForWholeDocument(String resourceId,
                                                                  String accessorIds,
                                                                  ResourceDefinition resourceDefinition,
                                                                  Set<Permission> permissions) {
        return createGrant(
            resourceId, accessorIds, resourceDefinition, createPermissionsForWholeDocument(permissions));
    }

    public static ExplicitAccessGrant createGrantForWholeDocument(String resourceId,
                                                                  Set<String> accessorIds,
                                                                  ResourceDefinition resourceDefinition,
                                                                  Set<Permission> permissions) {
        return createGrant(
            resourceId, accessorIds, ROLE_NAME, resourceDefinition, createPermissionsForWholeDocument(permissions));
    }

    public static ExplicitAccessGrant createGrant(String resourceId,
                                                  String accessorIds,
                                                  ResourceDefinition resourceDefinition,
                                                  Map<JsonPointer, Set<Permission>> attributePermissions) {
        return createGrant(
            resourceId, ImmutableSet.of(accessorIds), ROLE_NAME, resourceDefinition, attributePermissions);
    }

    public static ExplicitAccessGrant createGrant(String resourceId,
                                                  String accessorIds,
                                                  String relationship,
                                                  ResourceDefinition resourceDefinition,
                                                  Map<JsonPointer, Set<Permission>> attributePermissions) {
        return createGrant(
            resourceId, ImmutableSet.of(accessorIds), relationship, resourceDefinition, attributePermissions);
    }

    public static ExplicitAccessGrant createGrant(String resourceId,
                                                  Set<String> accessorIds,
                                                  String relationship,
                                                  ResourceDefinition resourceDefinition,
                                                  Map<JsonPointer, Set<Permission>> attributePermissions) {
        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(accessorIds)
            .accessorType(USER)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(attributePermissions)
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

    public static ExplicitAccessMetadata createMetadata(String resourceId, String accessorId,
                                                        ResourceDefinition resourceDefinition) {
        return createMetadata(resourceId, accessorId, ROLE_NAME, resourceDefinition, JsonPointer.valueOf(""));
    }

    public static ExplicitAccessMetadata createMetadata(String resourceId, String accessorId,
                                                        String relationship, ResourceDefinition resourceDefinition,
                                                        JsonPointer attribute) {
        return ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(USER)
            .resourceDefinition(resourceDefinition)
            .attribute(attribute)
            .relationship(relationship)
            .build();
    }

    public static Resource createResource(String resourceId, ResourceDefinition resourceDefinition) {
        return Resource.builder()
            .id(resourceId)
            .definition(resourceDefinition)
            .data(DATA)
            .build();
    }
}
