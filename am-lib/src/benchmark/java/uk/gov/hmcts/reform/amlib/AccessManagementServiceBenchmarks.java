package uk.gov.hmcts.reform.amlib;

import com.google.common.collect.ImmutableSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.states.BenchmarkState;
import uk.gov.hmcts.reform.amlib.states.DataState;

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

        benchmark.service.filterResource(accessorId, ImmutableSet.of(accessorRole), Resource.builder()
            .id(resourceId)
            .definition(resourceDefinition)
            .data(data.resourceDataFor(resourceDefinition.getServiceName()))
            .build());
    }
}
