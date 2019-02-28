package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.amlib.enums.Permission;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                }, (firstPointer, secondPointer) -> firstPointer);
    }

    private List<JsonPointer> filterPointersWithoutReadPermission(Map<JsonPointer, Set<Permission>> attributePermissions) {
        return attributePermissions.entrySet().stream()
            .filter(entry -> !entry.getValue().contains(READ))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private void retainFieldsWithReadPermission(JsonNode resource, List<JsonPointer> uniqueNodesWithRead) {
        uniqueNodesWithRead.forEach(pointerCandidateForRetaining -> {
            log.debug(">> Pointer candidate for retaining: " + pointerCandidateForRetaining);
            JsonPointer fieldPointer = pointerCandidateForRetaining.last();
            JsonPointer parentPointer = pointerCandidateForRetaining.head();

            while (parentPointer != null) {
                JsonNode node = resource.at(parentPointer);
                if (node instanceof ObjectNode) {
                    log.debug(">>> Retaining '" + fieldPointer + "' out of '" + parentPointer + "'");
                    ((ObjectNode) node).retain(fieldPointer.toString().substring(1));
                }

                fieldPointer = parentPointer.last();
                parentPointer = parentPointer.head();
            }
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
