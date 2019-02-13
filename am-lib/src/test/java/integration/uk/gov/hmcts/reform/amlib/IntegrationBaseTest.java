package integration.uk.gov.hmcts.reform.amlib;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.enums.Permissions;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessMetadata;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessRecord;

import java.util.Set;

import static integration.uk.gov.hmcts.reform.amlib.Constants.ACCESSOR_ID;
import static integration.uk.gov.hmcts.reform.amlib.Constants.ACCESS_TYPE;
import static integration.uk.gov.hmcts.reform.amlib.Constants.EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS;
import static integration.uk.gov.hmcts.reform.amlib.Constants.RESOURCE_NAME;
import static integration.uk.gov.hmcts.reform.amlib.Constants.RESOURCE_TYPE;
import static integration.uk.gov.hmcts.reform.amlib.Constants.SECURITY_CLASSIFICATION;
import static integration.uk.gov.hmcts.reform.amlib.Constants.SERVICE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD")
public abstract class IntegrationBaseTest {

    // According to H2 docs DB_CLOSE_DELAY is required in order to keep open connection to db (on close, h2 drops db)
    private static final String JDBC_URL = "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final String H2_BACKUP_LOCATION = "/tmp/h2backup.sql";

    AccessManagementService ams;
    private static Jdbi jdbi;

    @BeforeClass
    public static void initDatabase() {
        jdbi = Jdbi.create(JDBC_URL, "sa", "");

        initSchema();
        createBackup();
    }

    @Before
    public void setup() {
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

    @After
    public void loadFromBackup() {
        jdbi.withHandle(handle -> {
            handle.execute("DROP ALL OBJECTS");
            return handle.execute("RUNSCRIPT FROM ?", H2_BACKUP_LOCATION);
        });
    }

    ExplicitAccessRecord createRecord(String resourceId,
                                      String accessorId,
                                      Set<Permissions> explicitPermissions) {
        return ExplicitAccessRecord.explicitAccessRecordBuilder()
            .resourceId(resourceId)
            .accessorId(accessorId)
            .explicitPermissions(explicitPermissions)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("")
            .securityClassification(SECURITY_CLASSIFICATION)
            .build();
    }

    ExplicitAccessMetadata removeRecord(String resourceId) {
        return ExplicitAccessMetadata.builder()
            .resourceId(resourceId)
            .accessorId(ACCESSOR_ID)
            .accessType(ACCESS_TYPE)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attribute("")
            .build();
    }

    void grantAndRevokeAccessToRecord(String resourceId) {
        createRecord(resourceId, ACCESSOR_ID, EXPLICIT_READ_CREATE_UPDATE_PERMISSIONS);
        removeRecord(resourceId);
    }

    int countResourcesById(String resourceId) {
        return jdbi.open().createQuery(
            "select count(1) from access_management where resource_id = ?")
            .bind(0, resourceId)
            .mapTo(int.class)
            .findOnly();
    }
}