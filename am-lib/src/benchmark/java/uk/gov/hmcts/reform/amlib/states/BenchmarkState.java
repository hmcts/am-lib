package uk.gov.hmcts.reform.amlib.states;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import uk.gov.hmcts.reform.amlib.AccessManagementService;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;

import static uk.gov.hmcts.reform.amlib.utils.DataSourceFactory.createDataSource;
import static uk.gov.hmcts.reform.amlib.utils.EnvironmentVariableUtils.getValueOrDefault;

@State(Scope.Benchmark)
public class BenchmarkState {
    public AccessManagementService service;

    @Setup
    public void populateDatabase() throws Throwable {
        if (getValueOrDefault("BENCHMARK_POPULATE_DATABASE", "true").toLowerCase().equals("true")) {
            String databaseScriptsLocation = "src/benchmark/resources/database-scripts";
            runScripts(
                Paths.get(databaseScriptsLocation + "/truncate.sql"),
                Paths.get(databaseScriptsLocation + "/populate/services.sql"),
                Paths.get(databaseScriptsLocation + "/populate/resources.sql"),
                Paths.get(databaseScriptsLocation + "/populate/roles.sql"),
                Paths.get(databaseScriptsLocation + "/populate/resource_attributes.sql"),
                Paths.get(databaseScriptsLocation + "/populate/default_permissions_for_roles.sql"),
                Paths.get(databaseScriptsLocation + "/populate/access_management.copy.sql"),
                Paths.get(databaseScriptsLocation + "/populate/access_management.sql")
            );
        }
    }

    @Setup
    public void initiateService() {
        service = new AccessManagementService(createDataSource());
    }

    private void runScripts(Path... scriptPaths) throws Throwable {
        runWithTimeTracking(() -> {
            try (Connection connection = createDataSource().getConnection()) {
                connection.setAutoCommit(false);
                for (Path scriptPath : scriptPaths) {
                    String fileName = scriptPath.getFileName().toString();
                    if (fileName.endsWith("copy.sql")) {
                        String tableName = fileName.substring(0, fileName.indexOf("."));
                        try (BufferedReader scriptReader = Files.newBufferedReader(scriptPath)) {
                            CopyManager copyManager = connection.unwrap(BaseConnection.class).getCopyAPI();
                            copyManager.copyIn("COPY public." + tableName + " FROM stdin", scriptReader);
                        }
                    } else {
                        try (Statement statement = connection.createStatement()) {
                            for (String scriptLine : Files.readAllLines(scriptPath)) {
                                statement.addBatch(scriptLine);
                            }
                            statement.executeBatch();
                        }
                    }
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
