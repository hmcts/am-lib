package uk.gov.hmcts.reform.amapi.functional;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.amapi.functional.client.AmApiClient;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import static java.lang.System.getenv;

@Slf4j
@TestPropertySource("classpath:application-functional.yaml")
@SuppressWarnings({"PMD.DataflowAnomalyAnalysis", "PMD.ConfusingTernary","PMD.ForLoopCanBeForeach",
    "PMD.LinguisticNaming"})
public class FunctionalTestSuite {

    @Value("${targetInstance}")
    protected String accessUrl;

    public AmApiClient amApiClient;

    @Before
    public void setUp() throws Exception {
        amApiClient = new AmApiClient(accessUrl);
        getFiles(new File(Thread.currentThread().getContextClassLoader().getResource("").getPath()));
        Path path = Paths.get("src\\functionalTest\\resources");
        List<Path> paths = new ArrayList<>();
        paths.add(path.resolve("delete-data.sql"));
        paths.add(path.resolve("load-data.sql"));

        try (Connection connection = createDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                for (Path pathFile : paths) {
                    for (String scriptLine : Files.readAllLines(pathFile)) {
                        statement.addBatch(scriptLine);
                    }
                    statement.executeBatch();
                }
            }
        } catch (Exception exe) {
            log.error("FunctionalTestSuite Data insertion error::" + exe.toString());
            throw exe;
        }
        log.info("Functional Data inserted::");
    }


    public void getFiles(File file) {
        File[] files;
        if (file.isFile()) {
            log.info("list of files::" + file.getAbsolutePath());

        } else {
            files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                getFiles(files[i]);
            }


        }
    }

    @SuppressWarnings({"deprecation"})
    public DataSource createDataSource() {
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
