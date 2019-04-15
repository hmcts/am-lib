package uk.gov.hmcts.reform.amlib.utils;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;

import static uk.gov.hmcts.reform.amlib.utils.DataSourceFactory.createDataSource;

public class DatabaseUtils {
    private DatabaseUtils() {
        throw new UnsupportedOperationException("Constructing utility class is not supported");
    }

    public static void runScripts(Path... scriptPaths) throws Throwable {
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

    private static void runWithTimeTracking(Runner runner) throws Throwable {
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
