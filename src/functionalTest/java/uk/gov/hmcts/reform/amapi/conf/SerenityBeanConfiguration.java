package uk.gov.hmcts.reform.amapi.conf;

import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import javax.sql.DataSource;

import static java.lang.System.getenv;

@Configuration
@SuppressWarnings({"PMD.ConfusingTernary"})
public class SerenityBeanConfiguration {

    @Bean
    DefaultRoleSetupImportService getDefaultRoleSetupImportService() {
        return new DefaultRoleSetupImportService(createDataSource());
    }


    @SuppressWarnings({"deprecation"})
    public DataSource createDataSource() {
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(getValueOrDefault("DATABASE_HOST", "localhost"));
        dataSource.setPortNumber(Integer.parseInt(getValueOrDefault("DATABASE_PORT", "5433")));
        dataSource.setDatabaseName(getValueOrThrow("DATABASE_NAME"));
        dataSource.setUser(getValueOrThrow("DATABASE_USER"));
        dataSource.setPassword(getValueOrThrow("DATABASE_PASS"));
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
