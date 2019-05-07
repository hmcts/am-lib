package integration.uk.gov.hmcts.reform.amlib.base;

import com.google.common.collect.ImmutableMap;
import integration.uk.gov.hmcts.reform.amlib.helpers.DatabaseHelperRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.testcontainers.containers.PostgreSQLContainer;

import java.lang.reflect.Constructor;

@SuppressWarnings({
    "PMD.AbstractClassWithoutAbstractMethod",
    "PMD.FieldNamingConventions",
    "PMD.AvoidThrowingRawExceptionTypes"
})
public abstract class IntegrationBaseTest {

    protected static final DatabaseHelperRepository databaseHelper;
    private static final PostgreSQLContainer db;

    static {
        db = createDatabaseContainer();
        db.start();
        initSchema();

        databaseHelper = Jdbi.create(db.getJdbcUrl(), db.getUsername(), db.getPassword())
            .installPlugin(new SqlObjectPlugin())
            .onDemand(DatabaseHelperRepository.class);

        databaseHelper.truncateTables();
    }

    protected static <T> T initService(Class<T> serviceClass) {
        try {
            Constructor<T> constructor = serviceClass.getConstructor(String.class, String.class, String.class);
            return constructor.newInstance(db.getJdbcUrl(), db.getUsername(), db.getPassword());
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate service " + serviceClass.getSimpleName(), e);
        }
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
