package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;

@Slf4j
public class FilterService {

    private static final JsonPointer WHOLE_RESOURCE_POINTER = JsonPointer.valueOf("");

    JsonNode filterJson(JsonNode resource, Map<JsonPointer, Set<Permission>> attributePermissions) {
        List<JsonPointer> nodesWithRead = filterPointersWithReadPermission(attributePermissions);
        log.debug("> Nodes with READ access: " + nodesWithRead);

        if (nodesWithRead.isEmpty()) {
            return null;
        }

        JsonNode resourceCopy = resource.deepCopy();

        if (!nodesWithRead.contains(WHOLE_RESOURCE_POINTER)) {
            List<JsonPointer> uniqueNodesWithRead = reducePointersToUniqueList(nodesWithRead);
            log.debug("> Unique nodes with READ access: " + uniqueNodesWithRead);

            retainFieldsWithReadPermission(resourceCopy, uniqueNodesWithRead);
        }

        List<JsonPointer> nodesWithoutRead = filterPointersWithoutReadPermission(attributePermissions);
        log.debug("> Nodes without READ access: " + nodesWithoutRead);

        removeFieldsWithoutReadPermission(resourceCopy, nodesWithRead, nodesWithoutRead);

        return resourceCopy;
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
                        pointerCandidate.toString().startsWith(acceptedPointer.toString())
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

    private void retainFieldsWithReadPermission(JsonNode resource, List<JsonPointer> uniqueNodesWithRead) {
        Map<Integer, Map<JsonPointer, Set<String>>> reduce = uniqueNodesWithRead.stream()
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

        log.debug(">> Pointer candidates for retaining: " + reduce.values());

        reduce.values().forEach(map -> {
            map.entrySet().forEach(entry -> {
                JsonNode node = resource.at(entry.getKey());
                if (node instanceof ObjectNode) {
                    log.debug(">>> Retaining '" + entry.getValue() + "' out of '" + entry.getKey() + "'");
                    ObjectNode filteredNode = ((ObjectNode) node).retain(entry.getValue());
                    if (filteredNode.size() == 0 && entry.getKey().head() != null) {
                        ((ObjectNode) resource.at(entry.getKey().head()))
                            .remove(entry.getKey().last().toString().substring(1));
                    }
                }
            });
        });
    }

    private void removeFieldsWithoutReadPermission(JsonNode resource, List<JsonPointer> nodesWithRead, List<JsonPointer> nodesWithoutRead) {
        nodesWithoutRead.forEach(pointerCandidateForRemoval -> {
            log.debug(">> Pointer candidate for removal: " + pointerCandidateForRemoval);
            List<JsonPointer> childPointersWithRead = nodesWithRead.stream()
                .filter(pointerWithRead -> pointerWithRead.toString().startsWith(pointerCandidateForRemoval.toString()))
                .collect(Collectors.toList());
            if (childPointersWithRead.isEmpty()) {
                // remove whole node
                JsonNode node = resource.at(pointerCandidateForRemoval.head());
                if (node instanceof ObjectNode) {
                    ((ObjectNode) node).remove(pointerCandidateForRemoval.last().toString().substring(1));
                }
            } else {
                // retain node's children with READ
                JsonNode node = resource.at(pointerCandidateForRemoval);
                if (node instanceof ObjectNode) {
                    ((ObjectNode) node).retain(childPointersWithRead.stream()
                        .map(pointer -> pointer.last().toString().substring(1))
                        .collect(Collectors.toList()));
                }
            }
        });
    }
}
