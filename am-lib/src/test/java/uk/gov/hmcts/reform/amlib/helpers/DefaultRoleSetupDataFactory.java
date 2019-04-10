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

import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_DEFINITION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;

@SuppressWarnings("LineLength")
public final class DefaultRoleSetupDataFactory {

    private DefaultRoleSetupDataFactory() {
        throw new UnsupportedOperationException("Constructing utility class is not supported");
    }

    public static Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>> createPermissionsForAttribute(JsonPointer attribute, Set<Permission> permissions) {
        return createPermissionsForAttribute(attribute, permissions, SecurityClassification.PUBLIC);
    }

    public static Map<JsonPointer, Map.Entry<Set<Permission>, SecurityClassification>> createPermissionsForAttribute(JsonPointer attribute, Set<Permission> permissions, SecurityClassification securityClassification) {
        Map.Entry<Set<Permission>, SecurityClassification> pair = new Pair<>(permissions, securityClassification);

        return ImmutableMap.of(attribute, pair);
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(Set<Permission> permissions) {
        return createDefaultPermissionGrant(ROOT_ATTRIBUTE, permissions);
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(JsonPointer attribute, Set<Permission> permissions) {
        return createDefaultPermissionGrant(attribute.toString(), permissions, RESOURCE_DEFINITION, ROLE_NAME);
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
            .attributePermissions(createPermissionsForAttribute(JsonPointer.valueOf(attribute), permissions))
            .build();
    }
}
