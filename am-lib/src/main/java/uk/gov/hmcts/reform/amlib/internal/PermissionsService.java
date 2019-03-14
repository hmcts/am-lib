package uk.gov.hmcts.reform.amlib.internal;

import com.fasterxml.jackson.core.JsonPointer;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PermissionsService {

    public Map<JsonPointer, Set<Permission>> mergePermissions(List<Map<JsonPointer, Set<Permission>>> permissions) {
        Map<JsonPointer, Set<Permission>> mergedAttributePermissions = mergePermissionMaps(permissions);
        propagateParentPermissionsToChildren(mergedAttributePermissions);
        return mergedAttributePermissions;
    }

    private Map<JsonPointer, Set<Permission>> mergePermissionMaps(List<Map<JsonPointer, Set<Permission>>> permissions) {
        return permissions.stream()
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                this::mergePermissions,
                () -> new TreeMap<>(Comparator.comparing(Object::toString))
            ));
    }

    private void propagateParentPermissionsToChildren(Map<JsonPointer, Set<Permission>> attributePermissions) {
        attributePermissions.forEach((key, value) -> {
            JsonPointer head = key.head();

            while (head != null) {
                if (attributePermissions.containsKey(head)) {
                    attributePermissions.put(key, mergePermissions(value, attributePermissions.get(head)));
                }

                head = head.head();
            }
        });
    }

    private Set<Permission> mergePermissions(Set<Permission> v1, Set<Permission> v2) {
        return Stream.concat(v1.stream(), v2.stream()).collect(Collectors.toSet());
    }
}

