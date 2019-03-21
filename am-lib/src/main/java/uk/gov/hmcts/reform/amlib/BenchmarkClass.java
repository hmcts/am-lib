package uk.gov.hmcts.reform.amlib;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;

public class BenchmarkClass {

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 1, warmups = 1)
    public void testGrantExplicitResourceAccess(SpikeSetup setup) {
        setup.service.grantExplicitResourceAccess(setup.grant);
    }
}
