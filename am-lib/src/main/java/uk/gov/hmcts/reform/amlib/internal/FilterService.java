package uk.gov.hmcts.reform.amlib.internal;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.NONE;

@Slf4j
@SuppressWarnings("LineLength")
public class FilterService {

    private static final JsonPointer WHOLE_RESOURCE_POINTER = JsonPointer.valueOf("");

    public JsonNode filterJson(JsonNode resource,
                               Map<JsonPointer, Set<Permission>> attributePermissions/*,
                               Map<JsonPointer, SecurityClassification> attributeSecurityClassifications,
                               Set<SecurityClassification> userSecurityClassifications*/) {

        List<JsonPointer> nodesToKeep = filterPointersWithReadPermission(attributePermissions);
        log.debug("> Nodes with READ access: " + nodesToKeep);

        if (nodesToKeep.isEmpty()) {
            return null;
        }

        JsonNode resourceCopy = resource.deepCopy();

        if (!nodesToKeep.contains(WHOLE_RESOURCE_POINTER)) {
            List<JsonPointer> uniqueNodesWithRead = reducePointersToUniqueList(nodesToKeep);
            log.debug("> Unique nodes with READ access: " + uniqueNodesWithRead);

            retainFieldsWithReadPermission(resourceCopy, uniqueNodesWithRead);
        }

        List<JsonPointer> nodesToRemove = filterPointersWithoutReadPermission(attributePermissions);
        log.debug("> Nodes without READ access: " + nodesToRemove);

        /*filterNodesBySecurityClassification(nodesToKeep, nodesToRemove, attributeSecurityClassifications,
            userSecurityClassifications);*/

        removeFieldsFromData(resourceCopy, nodesToKeep, nodesToRemove);

        return resourceCopy;
    }

    /*private void filterNodesBySecurityClassification(List<JsonPointer> nodesToKeep,
                                                     List<JsonPointer> nodesToRemove,
                                                     Map<JsonPointer, SecurityClassification>
                                                         attributeSecurityClassifications,
                                                     Set<SecurityClassification> userSecurityClassifications) {
        nodesToKeep.forEach(node -> {
            SecurityClassification nodeSecurityClassification = attributeSecurityClassifications.get(node);
            if (nodeSecurityClassification == null) {
                nodeSecurityClassification = inheritAttributeSecurityClassification(
                    node, attributeSecurityClassifications);
            }

            if (!userSecurityClassifications.contains(nodeSecurityClassification)) {
                log.debug("> Node without security classification access: " + node);
                nodesToKeep.remove(node);
                nodesToRemove.add(node);
            }
        });
    }*/

    private SecurityClassification inheritAttributeSecurityClassification(JsonPointer attribute,
                                                                          Map<JsonPointer, SecurityClassification>
                                                                              attributeSecurityClassifications) {
        JsonPointer parentAttribute = attribute.head();
        while (attributeSecurityClassifications.get(parentAttribute) == null) {
            if (parentAttribute.toString().equals("")) {
                return NONE;
            }
            parentAttribute = parentAttribute.head();
        }
        return attributeSecurityClassifications.get(parentAttribute);
    }

    private List<JsonPointer> filterPointersWithReadPermission(Map<JsonPointer, Set<Permission>> attributePermissions) {
        return attributePermissions.entrySet().stream()
            .filter(entry -> entry.getValue().contains(READ))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private List<JsonPointer> reducePointersToUniqueList(List<JsonPointer> nodesWithRead) {
        return nodesWithRead.stream()
            .sorted(Comparator.comparing(JsonPointer::toString))
            .reduce(new ArrayList<>(),
                (List<JsonPointer> result, JsonPointer pointerCandidate) -> {
                    // already contains parent so no point adding
                    if (result.stream().noneMatch(acceptedPointer ->
                        pointerCandidate.toString().startsWith(acceptedPointer.toString() + "/")
                    )) {
                        result.add(pointerCandidate);
                    }
                    return result;
                }, (f, s) -> f);
    }

    private List<JsonPointer> filterPointersWithoutReadPermission(Map<JsonPointer, Set<Permission>> attributePermissions) {
        return attributePermissions.entrySet().stream()
            .filter(entry -> !entry.getValue().contains(READ))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap") // Sorted map is needed; instance is local as well
    private void retainFieldsWithReadPermission(JsonNode resource, List<JsonPointer> uniqueNodesWithRead) {

        Collection<Map<JsonPointer, Set<String>>> pointersByDepth = decomposePointersByDepth(uniqueNodesWithRead).values();
        log.debug(">> Pointer candidates for retaining: " + pointersByDepth);

        pointersByDepth.forEach(map -> map.forEach((key, value) -> {
            JsonNode node = resource.at(key);
            if (node instanceof ObjectNode) {
                log.debug(">>> Retaining '" + value + "' out of '" + key + "'");
                ObjectNode filteredNode = ((ObjectNode) node).retain(value);
                if (filteredNode.size() == 0 && key.head() != null) {
                    ((ObjectNode) resource.at(key.head()))
                        .remove(key.last().toString().substring(1));
                }
            }
        }));
    }

    /**
     * Decomposes set of JSON pointers into parent-children maps sorted by the depth of the tree.
     *
     * <p>Example:
     * <pre>
     * [
     *   /claimant/name
     *   /claimant/address
     *   /defendant/address
     * ]
     *
     * {
     *   "2": {
     *     "/claimant": [
     *       ["name", "address"]
     *     ],
     *     "/defendant": [
     *       ["address"]
     *     ]
     *   },
     *   "1": {
     *     "/": ["claimant", "defendant"]
     *   }
     * }
     * </pre>
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // Objects cannot be created outside the loop
    private Map<Integer, Map<JsonPointer, Set<String>>> decomposePointersByDepth(List<JsonPointer> nodes) {
        return nodes.stream()
            .reduce(new TreeMap<>(Collections.reverseOrder()),
                (Map<Integer, Map<JsonPointer, Set<String>>> result, JsonPointer pointer) -> {
                    JsonPointer fieldPointer = pointer.last();
                    JsonPointer parentPointer = pointer.head();

                    while (parentPointer != null) {
                        int depth = parentPointer.toString().split("/").length;

                        if (!result.containsKey(depth)) {
                            result.put(depth, new ConcurrentHashMap<>());
                        }

                        Map<JsonPointer, Set<String>> map = result.get(depth);

                        if (!map.containsKey(parentPointer)) {
                            map.put(parentPointer, new HashSet<>());
                        }

                        map.get(parentPointer).add(fieldPointer.toString().substring(1));

                        fieldPointer = parentPointer.last();
                        parentPointer = parentPointer.head();
                    }

                    return result;
                }, (firstPointer, secondPointer) -> firstPointer);
    }

    private void removeFieldsFromData(JsonNode resource, List<JsonPointer> nodesToKeep, List<JsonPointer> nodesToRemove) {
        nodesToRemove.forEach(pointerCandidateForRemoval -> {
            log.debug(">> Pointer candidate for removal: " + pointerCandidateForRemoval);
            List<JsonPointer> childPointersWithRead = nodesToKeep.stream()
                .filter(pointerWithRead -> pointerWithRead.toString().startsWith(pointerCandidateForRemoval.toString() + "/"))
                .collect(Collectors.toList());
            if (childPointersWithRead.isEmpty()) {
                // remove whole node
                log.debug(">>> Removing '" + pointerCandidateForRemoval + "'");
                JsonNode node = resource.at(pointerCandidateForRemoval.head());
                if (node instanceof ObjectNode) {
                    ((ObjectNode) node).remove(pointerCandidateForRemoval.last().toString().substring(1));
                }
            } else {
                // retain node's children with READ
                JsonNode node = resource.at(pointerCandidateForRemoval);
                if (node instanceof ObjectNode) {
                    childPointersWithRead.forEach(childPointerWithRead -> {
                        JsonPointer fieldPointer = childPointerWithRead.last();
                        JsonPointer parentPointer = childPointerWithRead.head();

                        while (!Objects.equals(parentPointer, pointerCandidateForRemoval.head())) {
                            log.debug(">>> Retaining '" + fieldPointer + "' out of '" + parentPointer + "'");
                            ((ObjectNode) resource.at(parentPointer)).retain(fieldPointer.toString().substring(1));

                            fieldPointer = parentPointer.last();
                            parentPointer = parentPointer.head();
                        }
                    });
                }
            }
        });
    }
}
