package integration.uk.gov.hmcts.reform.amlib.base;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

@SuppressWarnings("PMD")
public abstract class IntegrationBaseTest {

    private static final PostgreSQLContainer db = new PostgreSQLContainer().withUsername("sa").withPassword("");
    private static Jdbi jdbi;
    protected AccessManagementService ams;
    protected DefaultRoleSetupImportService defaultRoleService;

    @BeforeAll
    static void initDatabase() {
        db.start();
        jdbi = Jdbi.create(db.getJdbcUrl(), db.getUsername(), db.getPassword());

        initSchema();
    }

    @AfterAll
    static void destroyDatabase() {
        db.stop();
    }

    @BeforeEach
    void setup() {
        ams = new AccessManagementService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
        defaultRoleService = new DefaultRoleSetupImportService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
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

    protected static int countResourcesById(String resourceId) {
        return jdbi.open().createQuery(
            "select count(1) from access_management where resource_id = ?")
            .bind(0, resourceId)
            .mapTo(int.class)
            .findOnly();
    }

    protected static int countServices(String serviceName) {
        return jdbi.open().createQuery(
            "select count(1) from services where services.service_name = ?")
            .bind(0, serviceName)
            .mapTo(int.class)
            .findOnly();
    }
}
