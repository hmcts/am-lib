package uk.gov.hmcts.reform.amlib;

import com.google.common.collect.ImmutableSet;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import uk.gov.hmcts.reform.amlib.states.BenchmarkState;
import uk.gov.hmcts.reform.amlib.states.DataState;

@BenchmarkMode(Mode.Throughput)
@SuppressWarnings({"PMD.NonStaticInitializer", "PMD.EmptyCatchBlock"})
public class AccessManagementServiceBenchmarks {

    @Benchmark
    public void filterResourceBenchmark(BenchmarkState benchmark, DataState data) {
        benchmark.service.filterResource(data.accessorId, ImmutableSet.of("DOES_NOT_EXIST"), data.resource);
    }
}
