package uk.gov.hmcts.reform.amlib;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {
    public static void main(String[] args) throws RunnerException {
            Options opt = new OptionsBuilder()
                .include(".*")
                .warmupIterations(10)
                .measurementIterations(100)
                .build();

            new Runner(opt).run();
        }
}
