package uk.gov.hmcts.reform.amapi.conf;

import org.postgresql.ds.PGSimpleDataSource;
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

    public DataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName(getValueOrDefault("DATABASE_HOST", "localhost"));
        dataSource.setPortNumber(Integer.parseInt(getValueOrDefault("DATABASE_PORT", "5433")));
        dataSource.setDatabaseName(getValueOrThrow("DATABASE_NAME"));
        dataSource.setUser(getValueOrThrow("DATABASE_USER"));
        dataSource.setPassword(getValueOrThrow("DATABASE_PASS"));

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
