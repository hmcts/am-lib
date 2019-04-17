package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.Pair;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

public final class DefaultRoleSetupDataFactory {

    private DefaultRoleSetupDataFactory() {
        throw new UnsupportedOperationException("Constructing utility class is not supported");
    }

    public static Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>> createPermissionsForAttribute(
        JsonPointer attribute, Set<Permission> permissions) {

        Map.Entry<Set<Permission>, SecurityClassification> pair =
            new Pair<>(permissions, SecurityClassification.PUBLIC);

        return ImmutableMap.of(attribute, pair);
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(Set<Permission> permissions) {
        return DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .definition(ResourceDefinition.builder()
                .serviceName(SERVICE_NAME)
                .resourceType(RESOURCE_TYPE)
                .resourceName(RESOURCE_NAME)
                .build())
            .attributePermissions(createPermissionsForAttribute(ROOT_ATTRIBUTE, permissions))
            .build();
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(
        JsonPointer attribute, Set<Permission> permissions) {
        return DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .definition(ResourceDefinition.builder()
                .serviceName(SERVICE_NAME)
                .resourceType(RESOURCE_TYPE)
                .resourceName(RESOURCE_NAME)
                .build())
            .attributePermissions(createPermissionsForAttribute(attribute, permissions))
            .build();
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(String attribute,
                                                                      Set<Permission> permissions,
                                                                      ResourceDefinition resource,
                                                                      String roleName) {
        return DefaultPermissionGrant.builder()
            .roleName(roleName)
            .definition(ResourceDefinition.builder()
                .serviceName(resource.getServiceName())
                .resourceType(resource.getResourceType())
                .resourceName(resource.getResourceName())
                .build())
            .attributePermissions(createPermissionsForAttribute(JsonPointer.valueOf(attribute), permissions))
            .build();
    }

    public static ResourceDefinition buildResourceDefinition(String serviceName,
                                                             String resourceType,
                                                             String resourceName) {
        return ResourceDefinition.builder()
            .serviceName(serviceName)
            .resourceType(resourceType)
            .resourceName(resourceName)
            .build();
    }
}
