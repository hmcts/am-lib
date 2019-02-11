package integration.uk.gov.hmcts.reform.amlib;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.amlib.AccessManagementService;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD")
abstract class IntegrationBaseTest {

    // According to H2 docs DB_CLOSE_DELAY is required in order to keep open connection to db (on close, h2 drops db)
    private static final String JDBC_URL = "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final String H2_BACKUP_LOCATION = "/tmp/h2backup.sql";

    AccessManagementService ams;
    static Jdbi jdbi;

    @BeforeAll
    static void initDatabase() {
        jdbi = Jdbi.create(JDBC_URL, "sa", "");

        initSchema();
        createBackup();
    }

    @BeforeEach
    void setup() {
        ams = new AccessManagementService(JDBC_URL, "sa", "");
    }

    private static void initSchema() {
        FluentConfiguration configuration = new FluentConfiguration();
        configuration.dataSource(JDBC_URL, "sa", "");
        // Due sql migrations have to be in main resources, there are not added to classpath
        // so workaround is to pass relative path to them
        configuration.locations("filesystem:src/main/resources/db/migration");
        Flyway flyway = new Flyway(configuration);
        int noOfMigrations = flyway.migrate();
        assertThat(noOfMigrations).isGreaterThan(0);
    }

    private static void createBackup() {
        jdbi.withHandle(handle -> handle.execute("SCRIPT TO ?", H2_BACKUP_LOCATION));
    }

    @AfterEach
    void loadFromBackup() {
        jdbi.withHandle(handle -> {
            handle.execute("DROP ALL OBJECTS");
            return handle.execute("RUNSCRIPT FROM ?", H2_BACKUP_LOCATION);
        });
    }
}