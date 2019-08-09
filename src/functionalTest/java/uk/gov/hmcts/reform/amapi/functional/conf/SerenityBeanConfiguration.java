package uk.gov.hmcts.reform.amapi.functional.conf;

import lombok.extern.slf4j.Slf4j;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import javax.sql.DataSource;

import static java.lang.System.getenv;

@Configuration
@SuppressWarnings({"PMD.ConfusingTernary"})
@Slf4j
public class SerenityBeanConfiguration {

    @Bean
    DefaultRoleSetupImportService getDefaultRoleSetupImportService() {
        return new DefaultRoleSetupImportService(createDataSource());
    }

    @SuppressWarnings({"deprecation"})
    public DataSource createDataSource() {
        log.info("DB Host name::" + getValueOrDefault("AM_DB_HOST", "localhost"));
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(getValueOrDefault("AM_DB_NAME", "localhost"));
        dataSource.setPortNumber(Integer.parseInt(getValueOrDefault("AM_DB_PORT", "5433")));
        dataSource.setDatabaseName(getValueOrThrow("AM_DB_NAME"));
        dataSource.setUser(getValueOrThrow("AM_DB_USERNAME"));
        dataSource.setPassword(getValueOrThrow("AM_DB_PASSWORD"));
        dataSource.setMaxConnections(25);
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
