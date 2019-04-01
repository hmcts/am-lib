package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class AccessManagementServiceBenchmarkTest {

    private static final double REFERENCE_SCORE = 50.000;

    @Test
    void benchmarkRunner() throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(AccessManagementServiceBenchmarks.class.getSimpleName())
            .warmupIterations(2)
            .measurementIterations(5)
            .forks(0)
            .resultFormat(ResultFormatType.JSON)
            .result("performance.json")
            .addProfiler(StackProfiler.class)
            .jvmArgs("-prof")
            .build();

        Collection<RunResult> results = new Runner(opt).run();

        results.forEach(result -> assertTrue(result.getPrimaryResult().getScore() > REFERENCE_SCORE));
    }
}
