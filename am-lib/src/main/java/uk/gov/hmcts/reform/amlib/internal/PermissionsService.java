package uk.gov.hmcts.reform.amlib.internal;

import com.fasterxml.jackson.core.JsonPointer;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;

@Slf4j
public class PermissionsService {

    /**
     * Merges permission maps together by combining permissions by attribute. If an attribute is not present in all
     * sources then attribute merge is completed by propagating parent permissions from remaining sources onto a child.
     *
     * @param permissions list of permission maps representing for example permissions per user role
     * @return merged map of permissions per attribute
     */
    public Map<JsonPointer, Set<Permission>> merge(List<Map<JsonPointer, Set<Permission>>> permissions) {
        log.debug("Attempting to merge permission maps: {}", permissions);

        Map<JsonPointer, Merge> mergedAttributePermissions = combinePermissionByAttribute(permissions);
        propagateParentPermissionsToClosestChild(mergedAttributePermissions, permissions.size());
        return mergedAttributePermissions.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getCombinedPermissions()
            ));
    }

    /**
     * Merges permission maps together by combining permissions by attribute.
     *
     * @implNote
     * Map values not only represent combined permissions but also merge sources, represented as indexes
     * of the input list, that is needed later to complete merge by propagating permission from nearest parent.
     *
     * @param permissions list of permission maps representing for example permissions per user role
     * @return merged map of permissions per attribute sorted in reverse order by attribute name
     */
    @SuppressWarnings({
        "PMD.UseConcurrentHashMap", // TreeMap is used for a reason, concurrent map would not help
        "PMD.AvoidInstantiatingObjectsInLoops" // There is no good way to achieve the same in functional style
    })
    private Map<JsonPointer, Merge> combinePermissionByAttribute(List<Map<JsonPointer, Set<Permission>>> permissions) {
        Map<JsonPointer, Merge> mergedPermissions = new TreeMap<>(reverseOrder(comparing(Object::toString)));
        for (int i = 0; i < permissions.size(); i++) {
            int sourceIndex = i;

            Map<JsonPointer, Set<Permission>> permissionsPerRole = permissions.get(sourceIndex);
            permissionsPerRole.forEach((attribute, attributePermissionsPerRole) ->
                mergedPermissions.computeIfAbsent(attribute, value -> new Merge())
                    .permissions.put(sourceIndex, attributePermissionsPerRole));
        }
        return mergedPermissions;
    }

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts") // Refactoring would not help much in that case
    private void propagateParentPermissionsToClosestChild(Map<JsonPointer, Merge> attributePermissions,
                                                          int numberOfSources) {
        log.debug("> Attempting to propagate permissions onto child in pre-merged map: {}", attributePermissions);

        attributePermissions.forEach((attribute, attributeMerge) -> {
            if (attributeMerge.permissions.size() != numberOfSources) {
                log.debug(">> Attribute '{}' is not fully merged - it only has {} from {} sources", attribute,
                    attributeMerge.getCombinedPermissions(), attributeMerge.getSourceIndexes());

                JsonPointer head = attribute.head();
                while (head != null) {
                    if (attributePermissions.containsKey(head)) {
                        Merge parentMerge = attributePermissions.get(head);

                        Map<Integer, Set<Permission>> missingPermissions = findMissingPermissions(
                            parentMerge.permissions, attributeMerge.getSourceIndexes());

                        if (!missingPermissions.isEmpty()) {
                            log.debug(">>> Applying permissions {} from attribute '{}' to attribute '{}'",
                                missingPermissions, head, attribute);
                            attributePermissions.get(attribute).permissions.putAll(missingPermissions);
                            return;
                        }
                    }

                    head = head.head();
                }
            }
        });
    }

    /**
     * Finds permissions from sources that are not blacklisted.
     *
     * @param permissions map between source index and their permissions
     * @param blacklistedSourceIndexes set of blacklisted source indexes
     * @return map of permissions from non blacklisted sources
     */
    private Map<Integer, Set<Permission>> findMissingPermissions(Map<Integer, Set<Permission>> permissions,
                                                                 Set<Integer> blacklistedSourceIndexes) {
        return permissions.entrySet().stream()
            .filter(entry -> {
                Integer sourceIndex = entry.getKey();
                return !blacklistedSourceIndexes.contains(sourceIndex);
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Data
    @ToString
    private class Merge {
        private final Map<Integer, Set<Permission>> permissions = new ConcurrentHashMap<>();

        private Set<Integer> getSourceIndexes() {
            return permissions.keySet();
        }

        private Set<Permission> getCombinedPermissions() {
            return permissions.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        }
    }
}
