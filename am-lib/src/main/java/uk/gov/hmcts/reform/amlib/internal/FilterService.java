package uk.gov.hmcts.reform.amlib.internal;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
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

@Slf4j
@SuppressWarnings("LineLength")
public class FilterService {

    private static final JsonPointer ROOT_ATTRIBUTE = JsonPointer.valueOf("");

    public JsonNode filterJson(JsonNode resource,
                               Map<JsonPointer, Set<Permission>> attributePermissions,
                               Map<JsonPointer, SecurityClassification> attributeSecurityClassifications,
                               Set<SecurityClassification> userSecurityClassifications) {

        JsonNode resourceCopy = resource.deepCopy();
        ResourceMutationLists mutationLists = new ResourceMutationLists();

        createMutationListsBasedOnPermissions(mutationLists, attributePermissions);

        modifyMutationListsBasedOnSecurityClassifications(mutationLists,
            attributeSecurityClassifications, userSecurityClassifications);

        if (!mutationLists.getNodesToRetain().contains(ROOT_ATTRIBUTE)) {
            List<JsonPointer> uniqueNodesToRetain = reducePointersToUniqueList(mutationLists.getNodesToRetain());
            log.debug("> Unique nodes with visibility: " + uniqueNodesToRetain);

            retainVisibleFields(resourceCopy, uniqueNodesToRetain);
        }

        if (mutationLists.getNodesToRetain().isEmpty()) {
            return null;
        }

        removeFieldsFromData(resourceCopy, mutationLists);

        return resourceCopy;
    }

    private void createMutationListsBasedOnPermissions(ResourceMutationLists mutationLists,
                                                       Map<JsonPointer, Set<Permission>> attributePermissions) {
        attributePermissions.forEach((attribute, permissions) -> {
            if (permissions.contains(READ)) {
                mutationLists.addNodeToRetain(attribute);
                log.debug("> Node with READ access: " + attribute.toString());
            } else {
                mutationLists.addNodeToDelete(attribute);
                log.debug("> Node without READ access: " + attribute.toString());
            }
        });
    }

    private void modifyMutationListsBasedOnSecurityClassifications(
        ResourceMutationLists mutationLists, Map<JsonPointer, SecurityClassification> attributeSecurityClassifications,
        Set<SecurityClassification> userSecurityClassifications) {

        List<JsonPointer> nodesWithRead = new ArrayList<>(mutationLists.getNodesToRetain());
        nodesWithRead.forEach(node -> {

            // get attribute security classification
            SecurityClassification nodeSecurityClassification = attributeSecurityClassifications.get(node);
            if (nodeSecurityClassification == null) {
                nodeSecurityClassification = inheritAttributeSecurityClassificationFromParent(
                    node, attributeSecurityClassifications);
            }

            // if insufficient security classification, move to list of nodes to delete
            if (!userSecurityClassifications.contains(nodeSecurityClassification)) {
                mutationLists.moveNodeFromRetainListToDeleteList(node);
                log.debug("> Node without security classification access: " + node);
            }
        });
    }

    private SecurityClassification inheritAttributeSecurityClassificationFromParent(
        JsonPointer attribute, Map<JsonPointer, SecurityClassification> attributeSecurityClassifications) {

        JsonPointer parentAttribute = attribute.head();
        while (attributeSecurityClassifications.get(parentAttribute) == null) {
            parentAttribute = parentAttribute.head();
            if (parentAttribute.toString().isEmpty()) {
                break;
            }
        }
        return attributeSecurityClassifications.get(parentAttribute);
    }

    private List<JsonPointer> reducePointersToUniqueList(List<JsonPointer> visibleNodes) {
        return visibleNodes.stream()
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

    @SuppressWarnings("PMD.UseConcurrentHashMap") // Sorted map is needed; instance is local as well
    private void retainVisibleFields(JsonNode resource, List<JsonPointer> uniqueVisibleNodes) {

        Collection<Map<JsonPointer, Set<String>>> pointersByDepth = decomposePointersByDepth(uniqueVisibleNodes).values();
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

    /**
     * Filters a resource based on a list of nodes to be retained and a list of nodes to be deleted.
     * Nodes marked for deletion may carry child nodes which are marked for retention; in this situation,
     * the parent node is retained along with all other relative child nodes which are on the retention
     * list. Any other relative child nodes spawning from that parent node are deleted.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void removeFieldsFromData(JsonNode resource, ResourceMutationLists mutationLists) {
        mutationLists.getNodesToDelete().forEach(pointerCandidateForDeletion -> {
            log.debug(">> Node candidate for removal: " + pointerCandidateForDeletion);

            List<JsonPointer> visibleChildPointers = mutationLists.getNodesToRetain().stream()
                .filter(visiblePointer -> visiblePointer.toString().startsWith(pointerCandidateForDeletion.toString() + "/"))
                .collect(Collectors.toList());

            // if attribute has no visible child nodes, delete node from data
            if (visibleChildPointers.isEmpty()) {
                log.debug(">>> Removing '" + pointerCandidateForDeletion + "'");
                JsonNode node = resource.at(pointerCandidateForDeletion.head());
                if (node instanceof ObjectNode) {
                    ((ObjectNode) node).remove(pointerCandidateForDeletion.last().toString().substring(1));
                }

            // if attribute has visible child nodes, retain only visible children
            } else {
                log.debug(">>> Removing invisible child nodes from '" + pointerCandidateForDeletion + "'");
                JsonNode node = resource.at(pointerCandidateForDeletion);
                if (node instanceof ObjectNode) {
                    Map<JsonPointer, Set<String>> visibleNodes = new ConcurrentHashMap<>();
                    visibleChildPointers.forEach(visibleChildPointer -> {

                        // for each visible child node, add to visible list along with all parent nodes up to attribute
                        JsonPointer fieldPointer;
                        JsonPointer parentPointer = visibleChildPointer;
                        do {
                            fieldPointer = parentPointer.last();
                            parentPointer = parentPointer.head();
                            visibleNodes.computeIfAbsent(parentPointer, k -> new HashSet<>());
                            visibleNodes.get(parentPointer).add(fieldPointer.toString().substring(1));
                            log.debug(">>>> Retaining visible node '" + fieldPointer.toString() + "'");
                        } while (!Objects.equals(pointerCandidateForDeletion, parentPointer));
                    });

                    // retain only the nodes in visible list
                    visibleNodes.forEach((pointer, fields) -> ((ObjectNode) resource.at(pointer)).retain(fields));
                }
            }
        });
    }

    @Getter
    private class ResourceMutationLists {
        private final List<JsonPointer> nodesToRetain = new ArrayList<>();
        private final List<JsonPointer> nodesToDelete = new ArrayList<>();

        private void addNodeToRetain(JsonPointer node) {
            nodesToRetain.add(node);
        }

        private void addNodeToDelete(JsonPointer node) {
            nodesToDelete.add(node);
        }

        private void moveNodeFromRetainListToDeleteList(JsonPointer node) {
            nodesToRetain.remove(node);
            addNodeToDelete(node);
        }
    }
}
