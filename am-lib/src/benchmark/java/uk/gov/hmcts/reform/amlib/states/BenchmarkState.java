package uk.gov.hmcts.reform.amlib.states;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import uk.gov.hmcts.reform.amlib.FilterResourceService;
import uk.gov.hmcts.reform.amlib.utils.DatabaseUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static uk.gov.hmcts.reform.amlib.utils.DataSourceFactory.createDataSource;
import static uk.gov.hmcts.reform.amlib.utils.EnvironmentVariableUtils.getValueOrDefault;

@State(Scope.Benchmark)
public class BenchmarkState {
    public FilterResourceService service;

    @Setup
    public void populateDatabase() throws Throwable {
        if (getValueOrDefault("BENCHMARK_POPULATE_DATABASE", "true").toLowerCase().equals("true")) {
            Path databaseScriptsLocation = Paths.get("src/benchmark/resources/database-scripts");
            DatabaseUtils.runScripts(
                databaseScriptsLocation.resolve("truncate.sql"),
                databaseScriptsLocation.resolve("populate/services.sql"),
                databaseScriptsLocation.resolve("populate/resources.sql"),
                databaseScriptsLocation.resolve("populate/roles.sql"),
                databaseScriptsLocation.resolve("populate/resource_attributes.sql"),
                databaseScriptsLocation.resolve("populate/default_permissions_for_roles.sql"),
                databaseScriptsLocation.resolve("populate/access_management.copy.sql"),
                databaseScriptsLocation.resolve("populate/access_management.sql")
            );
        }
    }

    @Setup
    public void initiateService() {
        service = new FilterResourceService(createDataSource());
    }
}
