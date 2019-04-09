package uk.gov.hmcts.reform.amlib.states;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import uk.gov.hmcts.reform.amlib.AccessManagementService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static uk.gov.hmcts.reform.amlib.utils.DataSourceFactory.createDataSource;

@State(Scope.Benchmark)
public class BenchmarkState {
    public AccessManagementService service;

    @Setup
    public void initiateService() {
        service = new AccessManagementService(createDataSource());
    }

    @Setup
    public void populateDatabase() throws Throwable {
        runScript(Paths.get("src/benchmark/resources/populate-database.sql"));
    }

    @TearDown
    public void cleanupDatabase() throws Throwable {
        runScript(Paths.get("src/benchmark/resources/truncate-database.sql"));
    }

    private void runScript(Path scriptPath) throws Throwable {
        runWithTimeTracking(() -> {
            List<String> commands = Files.readAllLines(scriptPath);

            try (Connection connection = createDataSource().getConnection()) {
                connection.setAutoCommit(false);
                try (Statement statement = connection.createStatement()) {
                    for (String command : commands) {
                        statement.addBatch(command);
                    }
                    statement.executeBatch();
                }
                connection.commit();
            }
        });
    }

    private void runWithTimeTracking(Runner runner) throws Throwable {
        Instant startTime = Instant.now();
        System.out.println("Script execution started");

        runner.run();

        Instant completeTime = Instant.now();
        System.out.println("Script execution completed in " + Duration.between(startTime, completeTime));
    }

    @FunctionalInterface
    private interface Runner<E extends Throwable> {
        void run() throws E;
    }
}
