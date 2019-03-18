package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.Pair;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

public final class DefaultRoleSetupDataFactory {

    private DefaultRoleSetupDataFactory() {
        throw new UnsupportedOperationException("Constructing utility class is not supported");
    }

    public static Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>>
        createReadPermissionsForAttribute(JsonPointer attribute, Set<Permission> permissions) {

        Map.Entry<Set<Permission>, SecurityClassification> pair =
            new Pair<>(permissions, SecurityClassification.PUBLIC);

        Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>> attributePermission =
            new ConcurrentHashMap<>();

        attributePermission.put(attribute, pair);

        return attributePermission;
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(Set<Permission> permissions) {
        return DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(createReadPermissionsForAttribute(ROOT_ATTRIBUTE, permissions))
            .build();
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(
        JsonPointer attribute, Set<Permission> permissions) {
        return DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(createReadPermissionsForAttribute(attribute, permissions))
            .build();
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(String attribute,
                                                                      Set<Permission> permissions,
                                                                      ResourceDefinition resource,
                                                                      String roleName) {
        return DefaultPermissionGrant.builder()
            .roleName(roleName)
            .serviceName(resource.getServiceName())
            .resourceType(resource.getResourceType())
            .resourceName(resource.getResourceName())
            .attributePermissions(createReadPermissionsForAttribute(JsonPointer.valueOf(attribute), permissions))
            .build();
    }
}
