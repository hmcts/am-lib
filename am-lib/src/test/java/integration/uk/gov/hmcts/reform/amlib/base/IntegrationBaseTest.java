package integration.uk.gov.hmcts.reform.amlib.base;

import com.google.common.collect.ImmutableMap;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterAll;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;

import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD")
public abstract class IntegrationBaseTest {

    protected static final PostgreSQLContainer db;
    private static final Jdbi jdbi;

    static {
        db = createDatabaseContainer();
        db.start();
        initSchema();

        jdbi = Jdbi.create(db.getJdbcUrl(), db.getUsername(), db.getPassword());
    }

    @AfterAll
    static void cleanupDatabase() {
        jdbi.open().execute(
            "delete from access_management;"
                + "delete from default_permissions_for_roles;"
                + "delete from resource_attributes;"
                + "delete from resources;"
                + "delete from services;"
                + "delete from roles;"
        );
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

    protected static int countResourcesById(String resourceId) {
        return jdbi.open().createQuery(
            "select count(1) from access_management where resource_id = ?")
            .bind(0, resourceId)
            .mapTo(int.class)
            .findOnly();
    }

    protected static List<Map<String, Object>> countServices(String serviceName) {
        return jdbi.open().createQuery(
            "select * from services where services.service_name = ?")
            .bind(0, serviceName)
            .mapToMap()
            .list();
    }

    protected static List<Map<String, Object>> countRoles(String roleName) {
        return jdbi.open().createQuery(
            "select * from roles where roles.role_name = ?")
            .bind(0, roleName)
            .mapToMap()
            .list();
    }

    protected static int countResources(String serviceName, String resourceType, String resourceName) {
        return jdbi.open().createQuery(
            "select count(1) from resources where resources.service_name = ? and resources.resource_type = ? and"
                + " resources.resource_name = ?")
            .bind(0, serviceName)
            .bind(1, resourceType)
            .bind(2, resourceName)
            .mapTo(int.class)
            .findOnly();
    }

    protected static int countDefaultPermissions(String serviceName,
                                                 String resourceType,
                                                 String resourceName,
                                                 String attribute,
                                                 String roleName,
                                                 int permission) {
        return jdbi.open().createQuery(
            "select count(1) from default_permissions_for_roles where"
                + " default_permissions_for_roles.service_name = ?"
                + " and default_permissions_for_roles.resource_type = ?"
                + " and default_permissions_for_roles.resource_name = ?"
                + " and default_permissions_for_roles.attribute = ?"
                + " and default_permissions_for_roles.role_name = ?"
                + " and default_permissions_for_roles.permissions = ?")
            .bind(0, serviceName)
            .bind(1, resourceType)
            .bind(2, resourceName)
            .bind(3, attribute)
            .bind(4, roleName)
            .bind(5, permission)
            .mapTo(int.class)
            .findOnly();
    }

    protected static int countResourceAttributes(String serviceName,
                                                 String resourceType,
                                                 String resourceName,
                                                 String attribute,
                                                 SecurityClassification securityClassification) {
        return jdbi.open().createQuery(
            "select count(1) from resource_attributes where"
                + " resource_attributes.service_name = ?"
                + " and resource_attributes.resource_type = ?"
                + " and resource_attributes.resource_name = ?"
                + " and resource_attributes.attribute = ?"
                + " and resource_attributes.default_security_classification = cast(? as security_classification)")
            .bind(0, serviceName)
            .bind(1, resourceType)
            .bind(2, resourceName)
            .bind(3, attribute)
            .bind(4, securityClassification)
            .mapTo(int.class)
            .findOnly();
    }
}
