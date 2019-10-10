package uk.gov.hmcts.reform.amapi.functional;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.amapi.functional.client.AmApiClient;
import uk.gov.hmcts.reform.amapi.functional.client.S2sClient;
import uk.gov.hmcts.reform.amapi.models.FilterResource;
import uk.gov.hmcts.reform.amlib.enums.AccessorType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.ExplicitAccessGrant;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.lang.System.getenv;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;

@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
@ActiveProfiles("functional")
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.ConfusingTernary", "PMD.JUnit4TestShouldUseTestAnnotation",
                   "PMD.ExcessiveImports", "PMD.LawOfDemeter"})
@Component
public class FunctionalTestSuite {

    @Value("${targetInstance}")
    protected String accessUrl;

    protected AmApiClient amApiClient;

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;

    @Value("${s2s-secret}")
    protected String s2sSecret;

    @Value("${version:v1}")
    protected String version;

    protected String resourceId;
    protected String accessorId;
    protected final AccessorType accessorType = USER;
    protected final JsonPointer attribute = JsonPointer.valueOf("");
    protected static String resourceName = "claim-test";
    protected static String resourceType = "case-test";
    protected static String serviceName = "cmc-test";
    protected static String relationship = "caseworker-test";

    @BeforeClass
    public static void dbSetup() throws Exception {
        String loadFile = ResourceUtils.getFile("classpath:load-data-functional.sql").getCanonicalPath();
        String deleteFile = ResourceUtils.getFile("classpath:delete-data-functional.sql").getCanonicalPath();
        executeScript(ImmutableList.of(Paths.get(deleteFile), Paths.get(loadFile)));
    }

    @AfterClass
    public static void dbTearDown() throws Exception {
        String deleteFile = ResourceUtils.getFile("classpath:delete-data-functional.sql").getCanonicalPath();
        executeScript(ImmutableList.of(Paths.get(deleteFile)));
    }

    @Before
    public void testSetup() {
        log.info("Am api rest url::" + accessUrl);
        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sName);
        log.info("Configured S2S URL: " + s2sUrl);
        log.info("access url::" + accessUrl);
        log.info("environment script execution::" + getenv("environment_name"));

        String s2sToken = new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();
        amApiClient = new AmApiClient(accessUrl, version, s2sToken);

        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();
    }

    private static void executeScript(List<Path> scriptFiles) throws SQLException, IOException {
        // functional initial data load for aat is added with DB secrets
        // and for aks is added with flyway db/migrationAks
        if ("aat".equalsIgnoreCase(getenv("environment_name"))) {
            log.info("environment script execution started::");
            try (Connection connection = createDataSource().getConnection()) {
                try (Statement statement = connection.createStatement()) {
                    for (Path path : scriptFiles) {
                        for (String scriptLine : Files.readAllLines(path)) {
                            statement.addBatch(scriptLine);
                        }
                        statement.executeBatch();
                    }
                }
            } catch (Exception exe) {
                log.error("FunctionalTestSuite script execution error with script ::" + exe.toString());
                throw exe;
            }
            log.info("environment script execution completed::");
        }
    }

    private static DataSource createDataSource() {
        log.info("DB Host name::" + getValueOrDefault("DATABASE_HOST", "localhost"));
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName(getValueOrDefault("DATABASE_HOST", "localhost"));
        dataSource.setPortNumber(Integer.parseInt(getValueOrDefault("DATABASE_PORT", "5433")));
        dataSource.setDatabaseName(getValueOrThrow("DATABASE_NAME"));
        dataSource.setUser(getValueOrThrow("DATABASE_USER"));
        dataSource.setPassword(getValueOrThrow("DATABASE_PASS"));
        return dataSource;
    }

    private static String getValueOrDefault(String name, String defaultValue) {
        String value = getenv(name);
        return value != null ? value : defaultValue;
    }

    private static String getValueOrThrow(String name) {
        String value = getenv(name);
        if (value == null) {
            throw new IllegalArgumentException("Environment variable '" + name + "' is missing");
        }
        return value;
    }

    protected ExplicitAccessGrant createGenericExplicitAccessGrant() {
        return ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .lastUpdate(Instant.now())
                .build())
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(accessorType)
            .attributePermissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
            .relationship(relationship)
            .lastUpdate(Instant.now())
            .build();
    }

    protected FilterResource createGenericFilterResourceMetadata(
        String accessorIdCustom, String resourcdeIdCustom, String relationshipCustom) {
        if (accessorIdCustom != null) {
            accessorId = accessorIdCustom;
            relationship = relationshipCustom;
            resourceId = resourcdeIdCustom;
        }
        return FilterResource.builder()
            .userId(accessorId)
            .userRoles(ImmutableSet.of(relationship))
            .resource(Resource.builder()
                .id(resourceId)
                .definition(ResourceDefinition.builder()
                    .serviceName(serviceName)
                    .resourceName(resourceName)
                    .resourceType(resourceType)
                    .lastUpdate(Instant.now())
                    .build())
                .data(JsonNodeFactory.instance.objectNode())
                .build())
            .attributeSecurityClassification(ImmutableMap.of(JsonPointer.valueOf(""), PUBLIC))
            .build();
    }

    protected String resourceDefinitionToString(String serviceName, String resourceName, String resourceType) {
        return "{resourceName=" + resourceName + ", serviceName=" + serviceName + ", resourceType="
            + resourceType + "}";
    }

    protected void createExplicitGrantForFilterCase(String resourceId, String accessorId, AccessorType accessorType,
                                                    String relationship, Permission permission) {
        ExplicitAccessGrant explicitAccessGrant = ExplicitAccessGrant.builder()
            .resourceId(resourceId)
            .resourceDefinition(ResourceDefinition.builder()
                .serviceName(serviceName)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .lastUpdate(Instant.now())
                .build())
            .accessorIds(ImmutableSet.of(accessorId))
            .accessorType(accessorType)
            .relationship(relationship)
            .attributePermissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(permission)))
            .lastUpdate(Instant.now())
            .build();

        amApiClient.createResourceAccess(explicitAccessGrant).post(
            amApiClient.getAccessUrl() + "api/" + version + "/access-resource");
    }
}
