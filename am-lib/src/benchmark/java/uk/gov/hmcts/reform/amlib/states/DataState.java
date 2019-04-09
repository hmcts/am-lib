package uk.gov.hmcts.reform.amlib.states;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.io.IOException;
import java.util.UUID;

@State(Scope.Benchmark)
public class DataState {
    public String accessorId;
    public Resource resource;

    @Setup
    public void setup() throws IOException {
        accessorId = UUID.randomUUID().toString();
        resource = Resource.builder()
            .id(UUID.randomUUID().toString())
            .definition(ResourceDefinition.builder()
                .serviceName("Service 1")
                .resourceType("Resource Type 1")
                .resourceName("resource")
                .build())
            .data(new ObjectMapper().readTree(ClassLoader.getSystemResource("data.json")))
            .build();
    }
}
