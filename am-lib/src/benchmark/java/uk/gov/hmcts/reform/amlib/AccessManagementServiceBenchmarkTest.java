package uk.gov.hmcts.reform.amlib;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.amlib.utils.EnvironmentVariableUtils.getValueOrDefault;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@Slf4j
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
            .measurementIterations(parseInt(getValueOrDefault("BENCHMARK_MEASUREMENT_ITERATIONS", "50")))
            .threads(max(getRuntime().availableProcessors() / 2, 1))
            .forks(0)
            .shouldFailOnError(true)
            .resultFormat(ResultFormatType.JSON)
            .result(reportsDirectory + "/result.json")
            .addProfiler(StackProfiler.class)
            .jvmArgs("-prof")
            .build();

        Collection<RunResult> runResults = new Runner(options).run();
        runResults.forEach(result -> {
            log.debug("Benchmark test Score -",result.getPrimaryResult().getScore());
            log.debug("Benchmark test Score Confidence -",result.getPrimaryResult().getScoreConfidence());
            log.debug("Benchmark test Score Error-",result.getPrimaryResult().getScoreError());
            log.debug("Benchmark test Params-",result.getParams().toString());

            if (result.getParams().getMode() == Mode.Throughput) {
                assertTrue(result.getPrimaryResult().getScore() > REFERENCE_SCORE);
            }
        });

    }

}

