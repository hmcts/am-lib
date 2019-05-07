package uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.states.BenchmarkState;
import uk.gov.hmcts.reform.amlib.states.DataState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;

@BenchmarkMode(Mode.Throughput)
@SuppressWarnings({"PMD.NonStaticInitializer", "PMD.EmptyCatchBlock"})
public class AccessManagementServiceBenchmarks {

    @Benchmark
    public void filterResourceBenchmark(BenchmarkState benchmark, DataState data) {
        int id = data.randomId();
        ResourceDefinition resourceDefinition = data.randomResourceDefinition();

        String accessorId = "user-" + id;
        String accessorRole = "caseworker";
        String resourceId = resourceDefinition.getServiceName() + "-resource-" + id;
        Map<JsonPointer, SecurityClassification> securityClassifications = new HashMap<JsonPointer, SecurityClassification>() {
            {
                put(JsonPointer.valueOf(UUID.randomUUID().toString()), PUBLIC);
            }
        };

        benchmark.service.filterResource(accessorId, ImmutableSet.of(accessorRole), Resource.builder()
            .id(resourceId)
            .definition(resourceDefinition)
            .data(data.resourceDataFor(resourceDefinition.getServiceName()))
            .build(), securityClassifications);
    }
}
