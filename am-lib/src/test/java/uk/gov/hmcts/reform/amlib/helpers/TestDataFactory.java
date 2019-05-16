package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
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

public final class TestDataFactory {

    private TestDataFactory() {
        //NO-OP
    }

    public static ExplicitAccessGrant createGrantForWholeDocument(String resourceId,
                                                                  String accessorId,
                                                                  String relationship,
                                                                  ResourceDefinition resourceDefinition,
                                                                  Set<Permission> permissions) {
        return createGrant(
            resourceId, accessorId, relationship, resourceDefinition, createPermissions("", permissions));
    }

    public static ExplicitAccessGrant createGrant(String resourceId,
                                                  String accessorId,
                                                  String relationship,
                                                  ResourceDefinition resourceDefinition,
                                                  Map<JsonPointer, Set<Permission>> attributePermissions) {
        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(USER)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(attributePermissions)
            .relationship(relationship)
            .build();
    }

    public static Map<JsonPointer, Set<Permission>> createPermissions(String attribute, Set<Permission> permissions) {
        return ImmutableMap.of(JsonPointer.valueOf(attribute), permissions);
    }

    public static ExplicitAccessMetadata createMetadata(String resourceId, String accessorId, String relationship,
                                                        ResourceDefinition resourceDefinition, JsonPointer attribute) {
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

    public static Resource createResource(String resourceId, ResourceDefinition resourceDefinition, JsonNode data) {
        return Resource.builder()
            .id(resourceId)
            .definition(resourceDefinition)
            .data(data)
            .build();
    }
}
