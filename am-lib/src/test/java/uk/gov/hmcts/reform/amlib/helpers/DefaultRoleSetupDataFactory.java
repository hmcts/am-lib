package uk.gov.hmcts.reform.amlib.helpers;

import com.fasterxml.jackson.core.JsonPointer;
import javafx.util.Pair;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

public final class DefaultRoleSetupDataFactory {

    private DefaultRoleSetupDataFactory() {
        throw new UnsupportedOperationException("Constructing utility class is not supported");
    }

    public static Map<JsonPointer, Pair<Set<Permission>, SecurityClassification>> createReadPermissionsForAttribute(
        Set<Permission> permissions) {

        Pair<Set<Permission>, SecurityClassification> pair = new Pair<>(permissions, SecurityClassification.PUBLIC);
        Map<JsonPointer, Pair<Set<Permission>, SecurityClassification>> attributePermission = new ConcurrentHashMap<>();
        attributePermission.put(JsonPointer.valueOf(ATTRIBUTE), pair);

        return attributePermission;
    }

    public static DefaultPermissionGrant createDefaultPermissionGrant(Set<Permission> permissions) {
        return DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(createReadPermissionsForAttribute(permissions))
            .build();
    }
}
