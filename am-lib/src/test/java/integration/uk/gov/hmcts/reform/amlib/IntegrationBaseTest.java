package integration.uk.gov.hmcts.reform.amlib;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.reform.amlib.AccessManagementService;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD")
public abstract class IntegrationBaseTest {
    protected AccessManagementService ams;
    protected static Jdbi jdbi;

    @ClassRule
    public static final PostgreSQLContainer db = new PostgreSQLContainer().withUsername("sa").withPassword("");

    @BeforeClass
    public static void initDatabase() {
        jdbi = Jdbi.create(db.getJdbcUrl(), db.getUsername(), db.getPassword());

        initSchema();
    }

    @Before
    public void setup() {
        ams = new AccessManagementService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
    }

    private static void initSchema() {
        FluentConfiguration configuration = new FluentConfiguration();
        configuration.dataSource(db.getJdbcUrl(), db.getUsername(), db.getPassword());
        // Due sql migrations have to be in main resources, there are not added to classpath
        // so workaround is to pass relative path to them
        configuration.locations("filesystem:src/main/resources/db/migration");
        Flyway flyway = new Flyway(configuration);
        int noOfMigrations = flyway.migrate();
        assertThat(noOfMigrations).isGreaterThan(0);
    }
}