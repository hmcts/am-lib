package integration.uk.gov.hmcts.reform.amlib;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.junit.BeforeClass;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class IntegrationBaseTest {

    // According to H2 docs DB_CLOSE_DELAY is required in order to keep open connection to db (on close, h2 drops db)
    public static final String JDBC_URL = "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";

    protected static Jdbi jdbi;

    @BeforeClass
    public static void initDatabase() {
        jdbi = Jdbi.create(JDBC_URL, "sa", "");
        initSchema();
    }

    private static void initSchema() {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        FluentConfiguration configuration = new FluentConfiguration();
        configuration.dataSource(JDBC_URL, "sa", "");
        // Due sql migrations have to be in main resources, there are not added to classpath
        // so workaround is to pass relative path to them
        configuration.locations("filesystem:src/main/java/uk/gov/hmcts/reform/amlib/db/migration/");
        Flyway flyway = new Flyway(configuration);
        int noOfMigrations = flyway.migrate();
        assertThat(noOfMigrations).isGreaterThan(0);
    }
}