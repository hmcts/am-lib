package integration.uk.gov.hmcts.reform.amlib.base;

import com.google.common.collect.ImmutableMap;
import integration.uk.gov.hmcts.reform.amlib.helpers.DatabaseHelperRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("PMD")
public abstract class IntegrationBaseTest {

    protected static final PostgreSQLContainer db;
    protected static final DatabaseHelperRepository databaseHelper;

    static {
        db = createDatabaseContainer();
        db.start();
        initSchema();

        databaseHelper = Jdbi.create(db.getJdbcUrl(), db.getUsername(), db.getPassword())
            .installPlugin(new SqlObjectPlugin())
            .onDemand(DatabaseHelperRepository.class);
    }

    @AfterAll
    static void cleanupDatabase() {
        databaseHelper.truncateTables();
    }

    @SuppressWarnings("unchecked")
    private static PostgreSQLContainer createDatabaseContainer() {
        return (PostgreSQLContainer) new PostgreSQLContainer("postgres:10")
            .withUsername("sa")
            .withPassword("")
            .withTmpFs(ImmutableMap.of("/var/lib/postgresql/data", "rw"));
    }

    private static void initSchema() {
        FluentConfiguration configuration = new FluentConfiguration();
        configuration.dataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword());
        // Due sql migrations have to be in main resources, there are not added to classpath
        // so workaround is to pass relative path to them
        configuration.locations("filesystem:src/main/resources/db/migration");
        Flyway flyway = new Flyway(configuration);
        flyway.migrate();
    }
}
