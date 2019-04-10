package uk.gov.hmcts.reform.amlib.states;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.io.IOException;
import java.util.Random;

@State(Scope.Benchmark)
public class DataState {
    private static Random random = new Random();

    public JsonNode data;

    @Setup
    public void setup() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        data = objectMapper.readTree(ClassLoader.getSystemResource("data.json"));
    }

    public int randomId() {
        return random.nextInt(50000) + 1;
    }

    public ResourceDefinition resourceDefinition() {
        return ResourceDefinition.builder()
            .serviceName("fpl")
            .resourceType("case")
            .resourceName("application")
            .build();
    }
}
