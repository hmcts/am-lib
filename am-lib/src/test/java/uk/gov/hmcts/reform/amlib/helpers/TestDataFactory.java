package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.amlib.enums.AccessorType.ROLE;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_INSERTION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_REVOKE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CHANGED_BY_NAME_FOR_INSERTION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CHANGED_BY_NAME_FOR_REVOKE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UseObjectForClearerAPI"})
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
            .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
            .build();
    }

    public static ExplicitAccessGrant createGrantForAccessorType(String resourceId,
                                                                 String accessorId,
                                                                 String relationship,
                                                                 ResourceDefinition resourceDefinition,
                                                                 Map<JsonPointer, Set<Permission>> attributePermissions,
                                                                 AccessorType accessorType) {

        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(accessorType)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(attributePermissions)
            .relationship(relationship)
            .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
            .build();
    }

    public static ExplicitAccessGrant createGrantForRole(String resourceId,
                                                         String accessorId,
                                                         String relationship,
                                                         ResourceDefinition resourceDefinition,
                                                         Map<JsonPointer, Set<Permission>> attributePermissions) {
        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(ROLE)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(attributePermissions)
            .relationship(relationship)
            .callingServiceName(CALLING_SERVICE_NAME_FOR_INSERTION)
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
            .resourceType(resourceDefinition.getResourceType())
            .resourceName(resourceDefinition.getResourceName())
            .serviceName(resourceDefinition.getServiceName())
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

    public static ExplicitAccessGrant createExplicitAccessGrantWithAudit(String resourceId,
                                                                         String accessorId,
                                                                         String relationship,
                                                                         ResourceDefinition resourceDefinition,
                                                                         String callingServiceName) {
        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(USER)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
            .relationship(relationship)
            .callingServiceName(callingServiceName)
            .changedBy(CHANGED_BY_NAME_FOR_INSERTION)
            .build();
    }


    public static ExplicitAccessMetadata createMetadataForAudit(String resourceId, String accessorId,
                                                                String relationship,
                                                                ResourceDefinition resourceDefinition,
                                                                JsonPointer attribute) {

        return ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .accessorType(USER)
            .resourceType(resourceDefinition.getResourceType())
            .resourceName(resourceDefinition.getResourceName())
            .serviceName(resourceDefinition.getServiceName())
            .attribute(attribute)
            .relationship(relationship)
            .callingServiceName(CALLING_SERVICE_NAME_FOR_REVOKE)
            .changedBy(CHANGED_BY_NAME_FOR_REVOKE)
            .build();
    }
}
