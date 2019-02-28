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

    JsonNode filterJson(JsonNode resourceJson, Map<JsonPointer, Set<Permission>> attributePermissions) {
        List<JsonPointer> nodesWithRead = attributePermissions.entrySet().stream()
            .filter(entry -> entry.getValue().contains(READ))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        log.debug("> Nodes with READ access: " + nodesWithRead);

        if (nodesWithRead.isEmpty()) {
            return null;
        }

        List<JsonPointer> uniqueNodesWithRead = nodesWithRead.stream()
            .sorted(Comparator.comparing(JsonPointer::toString))
            .reduce(new ArrayList<>(),
                (List<JsonPointer> result, JsonPointer pointerCandidate) -> {
                    // already contains parent so no point adding
                    if (result.stream().noneMatch(acceptedPointer -> acceptedPointer.toString().equals("")
                        || pointerCandidate.toString().startsWith(acceptedPointer.toString())
                    )) {
                        result.add(pointerCandidate);
                    }
                    return result;
                }, (firstPointer, secondPointer) -> firstPointer);

        log.debug("> Unique nodes with READ access: " + uniqueNodesWithRead);

        JsonNode resourceCopy = resourceJson.deepCopy();

        if (!uniqueNodesWithRead.contains(WHOLE_RESOURCE_POINTER)) {
            uniqueNodesWithRead.forEach(pointerCandidateForRetaining -> {
                log.debug(">> Pointer candidate for retaining: " + pointerCandidateForRetaining);
                JsonPointer fieldPointer = pointerCandidateForRetaining.last();
                JsonPointer parentPointer = pointerCandidateForRetaining.head();

                while (parentPointer != null) {
                    ObjectNode node = (ObjectNode) resourceCopy.at(parentPointer);
                    log.debug(">>> Retaining '" + fieldPointer + "' out of '" + parentPointer + "'");
                    node.retain(fieldPointer.toString().substring(1));

                    fieldPointer = parentPointer.last();
                    parentPointer = parentPointer.head();
                }
            });
        }

        List<JsonPointer> nodesWithoutRead = attributePermissions.entrySet().stream()
            .filter(entry -> !entry.getValue().contains(READ))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        log.debug("> Nodes without READ access: " + nodesWithoutRead);

        nodesWithoutRead.forEach(pointerCandidateForRemoval -> {
            log.debug(">> Pointer candidate for removal: " + pointerCandidateForRemoval);
            List<JsonPointer> childPointersWithRead = nodesWithRead.stream()
                .filter(pointerWithRead -> pointerWithRead.toString().startsWith(pointerCandidateForRemoval.toString()))
                .collect(Collectors.toList());
            if (childPointersWithRead.isEmpty()) {
                // remove whole node
                ObjectNode node = (ObjectNode) resourceCopy.at(pointerCandidateForRemoval.head());
                node.remove(pointerCandidateForRemoval.last().toString().substring(1));
            } else {
                // retain node's children with READ
                ObjectNode node = (ObjectNode) resourceCopy.at(pointerCandidateForRemoval);
                node.retain(childPointersWithRead.stream()
                    .map(pointer -> pointer.last().toString().substring(1))
                    .collect(Collectors.toList()));
            }
        });

        return resourceCopy;
    }
}
