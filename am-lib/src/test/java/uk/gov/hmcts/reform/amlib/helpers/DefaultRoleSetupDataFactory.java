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

import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;

@SuppressWarnings("LineLength")
public final class DefaultRoleSetupDataFactory {

    private DefaultRoleSetupDataFactory() {
        throw new UnsupportedOperationException("Constructing utility class is not supported");
    }

    public static Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>> createPermissionsForAttribute(JsonPointer attribute, Set<Permission> permissions, SecurityClassification securityClassification) {
        Map.Entry<Set<Permission>, SecurityClassification> pair = new Pair<>(permissions, securityClassification);

        return ImmutableMap.of(attribute, pair);
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(ResourceDefinition resourceDefinition, Set<Permission> permissions) {
        return createDefaultPermissionGrant(resourceDefinition, permissions, JsonPointer.valueOf(""), PUBLIC);
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(ResourceDefinition resourceDefinition, JsonPointer attribute, Set<Permission> permissions) {
        return createDefaultPermissionGrant(resourceDefinition, permissions, attribute, PUBLIC);

    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(ResourceDefinition resourceDefinition, Set<Permission> permissions, JsonPointer attribute, SecurityClassification securityClassification) {
        return DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissionsForAttribute(attribute, permissions, securityClassification))
            .build();
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(ResourceDefinition resource, Set<Permission> permissions, String attribute, String roleName) {
        return DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(resource.getServiceName())
                .resourceType(resource.getResourceType())
                .resourceName(resource.getResourceName())
                .build())
            .attributePermissions(createPermissionsForAttribute(JsonPointer.valueOf(attribute), permissions, PUBLIC))
            .build();
    }

    public static ResourceDefinition createResourceDefinition(String serviceName,
                                                              String resourceType,
                                                              String resourceName) {
        return ResourceDefinition.builder()
            .serviceName(serviceName)
            .resourceType(resourceType)
            .resourceName(resourceName)
            .build();
    }
}
