package uk.gov.hmcts.reform.amlib;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.amlib.utils.EnvironmentVariableUtils.getValueOrDefault;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class AccessManagementServiceBenchmarkTest {
    private static final double REFERENCE_SCORE = 50;

    @Test
    void benchmarkScoreShouldBeGreaterThanThreshold() throws Exception {
        Path reportsDirectory = Paths.get("build/reports/jmh");
        if (Files.notExists(reportsDirectory)) {
            Files.createDirectory(reportsDirectory);
        }

        Options options = new OptionsBuilder()
            .include(AccessManagementServiceBenchmarks.class.getSimpleName())
            .warmupIterations(parseInt(getValueOrDefault("BENCHMARK_WARMUP_ITERATIONS", "4")))
            .measurementIterations(parseInt(getValueOrDefault("BENCHMARK_MEASUREMENT_ITERATIONS", "100")))
            .threads(max(getRuntime().availableProcessors() / 2, 1))
            .forks(0)
            .shouldFailOnError(true)
            .resultFormat(ResultFormatType.JSON)
            .result(reportsDirectory + "/result.json")
            .addProfiler(StackProfiler.class)
            .jvmArgs("-prof")
            .build();

        assertTrue(new Runner(options).run().stream()
            .allMatch(result -> result.getPrimaryResult().getScore() > REFERENCE_SCORE));
    }
}

