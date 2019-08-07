package uk.gov.hmcts.reform.amapi.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import static java.lang.System.getenv;

@Configuration
@SuppressWarnings({"PMD.ConfusingTernary"})
public class SerenityBeanConfiguration {

    @Bean
    DefaultRoleSetupImportService getDefaultRoleSetupImportService() {

        String username = getValueOrThrow("DATABASE_USER");
        String pass = getValueOrThrow("DATABASE_PASS");
        String url = "jdbc:postgresql://" + getValueOrDefault("DATABASE_HOST", "localhost") + ":"
            + getValueOrDefault("DATABASE_PORT", "5433") + "/" + getValueOrThrow("DATABASE_NAME");
        return new DefaultRoleSetupImportService(url,username,pass);
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
