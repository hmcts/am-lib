package uk.gov.hmcts.reform.amlib.states;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.BenchmarkException;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.amlib.utils.RandomNumberFactory.nextIntegerInRange;

@State(Scope.Benchmark)
public class DataState {
    private static ResourceDefinition[] definitions = new ResourceDefinition[]{
        ResourceDefinition.builder()
            .serviceName("fpl")
            .resourceType("case")
            .resourceName("application")
            .build(),
        ResourceDefinition.builder()
            .serviceName("cmc")
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

    public int randomId() {
        return nextIntegerInRange(1, 50000);
    }

    public ResourceDefinition randomResourceDefinition() {
        return definitions[nextIntegerInRange(0, definitions.length - 1)];
    }

    public JsonNode resourceDataFor(String serviceName) {
        return resourceDataPerService.get(serviceName);
    }
}
