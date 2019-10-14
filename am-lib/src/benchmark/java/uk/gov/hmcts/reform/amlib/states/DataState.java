package uk.gov.hmcts.reform.amlib.states;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.BenchmarkException;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.amlib.utils.RandomNumberFactory.nextIntegerInRange;

@State(Scope.Benchmark)
public class DataState {

    private static final List<SecurityClassification> VALUES =
        Collections.unmodifiableList(Arrays.asList(SecurityClassification.values()));

    private static ResourceDefinition[] definitions = new ResourceDefinition[]{
        ResourceDefinition.builder()
            .serviceName("fpl-jmhtest")
            .resourceType("case")
            .resourceName("application")
            .build(),
        ResourceDefinition.builder()
            .serviceName("cmc-jmhtest")
            .resourceType("case")
            .resourceName("claim")
            .build()
    };
    private static Map<String, JsonNode> resourceDataPerService = Arrays.stream(definitions)
        .collect(Collectors.toMap(
            ResourceDefinition::getServiceName,
            definition -> {
                try {
                    Path resourceDataLocation = Paths.get("src/benchmark/resources/resource-data");
                    Path resourceDataPath = resourceDataLocation.resolve(definition.getServiceName() + ".json");
                    try (Reader dataReader = Files.newBufferedReader(resourceDataPath)) {
                        return new ObjectMapper().readTree(dataReader);
                    }
                } catch (IOException ex) {
                    throw new BenchmarkException(ex);
                }
            }
        ));

    private static Map<String,Map<JsonPointer, SecurityClassification>> securityClassifications = Arrays
        .stream(definitions).collect(Collectors.toMap(ResourceDefinition::getServiceName,
            definition -> {
                try {
                    Path resourceDataLocation = Paths.get("src/benchmark/resources/resource-data");
                    Path resourceSecurityClassificationPath = resourceDataLocation
                        .resolve(definition.getServiceName() + "-securityclassification" + ".json");
                    try (Reader dataReader = Files.newBufferedReader(resourceSecurityClassificationPath)) {
                        Map<JsonPointer, SecurityClassification> securityClassificationMap =
                            new HashMap<JsonPointer, SecurityClassification>();
                        new ObjectMapper().readValue(dataReader, HashMap.class)
                            .forEach((jsonPointer,securityClassification) ->  securityClassificationMap
                                .put(JsonPointer.valueOf((String)jsonPointer),
                                    SecurityClassification.valueOf((String)securityClassification)));
                        return  securityClassificationMap;
                    }
                } catch (IOException ex) {
                    throw new BenchmarkException(ex);
                }
            }
        ));

    public int randomId() {
        return nextIntegerInRange(1, 50000);
    }

    public ResourceDefinition randomResourceDefinition() {
        return definitions[nextIntegerInRange(0, definitions.length - 1)];
    }

    public JsonNode resourceDataFor(String serviceName) {
        return resourceDataPerService.get(serviceName);
    }

    public Map<JsonPointer, SecurityClassification> getSecurityClassifications(String serviceName) {
        return securityClassifications.get(serviceName);
    }


}
