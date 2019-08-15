package uk.gov.hmcts.reform.amapi.functional;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.amapi.functional.client.AmApiClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import static java.lang.System.getenv;

@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.ConfusingTernary"})
@Component
public class FunctionalTestSuite {

    @Value("${targetInstance}")
    protected String accessUrl;

    public AmApiClient amApiClient;

    @Before
    public void setUp() throws Exception {
        log.info("Am api rest url::" + accessUrl);
        amApiClient = new AmApiClient(accessUrl);

        String loadFile = ResourceUtils.getFile("classpath:load-data-functional.sql").getCanonicalPath();
        String deleteFile = ResourceUtils.getFile("classpath:delete-data-functional.sql").getCanonicalPath();
        List<Path> files = new ArrayList<>();
        files.add(Paths.get(deleteFile));
        files.add(Paths.get(loadFile));

        executeScript(files);
    }

    private void executeScript(List<Path> scriptFiles) throws SQLException, IOException {
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
    }

    @After
    public void tearDown() throws Exception {

        String deleteFile = ResourceUtils.getFile("classpath:delete-data-functional.sql").getCanonicalPath();
        List<Path> files = new ArrayList<>();
        files.add(Paths.get(deleteFile));
        executeScript(files);
    }


    @SuppressWarnings({"deprecation"})
    public DataSource createDataSource() {
        log.info("DB Host name::" + getValueOrDefault("DATABASE_HOST", "localhost"));
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(getValueOrDefault("DATABASE_HOST", "localhost"));
        dataSource.setPortNumber(Integer.parseInt(getValueOrDefault("DATABASE_PORT", "5433")));
        dataSource.setDatabaseName(getValueOrThrow("DATABASE_NAME"));
        dataSource.setUser(getValueOrThrow("DATABASE_USER"));
        dataSource.setPassword(getValueOrThrow("DATABASE_PASS"));
        dataSource.setMaxConnections(5);
        return dataSource;
    }


    public static String getValueOrDefault(String name, String defaultValue) {
        String value = getenv(name);
        return value != null ? value : defaultValue;
    }

    public static String getValueOrThrow(String name) {
        String value = getenv(name);
        if (value == null) {
            throw new IllegalArgumentException("Environment variable '" + name + "' is missing");
        }
        return value;
    }

}
