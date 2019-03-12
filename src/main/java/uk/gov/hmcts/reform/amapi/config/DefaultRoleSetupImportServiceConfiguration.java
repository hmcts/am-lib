package uk.gov.hmcts.reform.amapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import javax.sql.DataSource;

@Configuration
public class DefaultRoleSetupImportServiceConfiguration {

    @Bean
    public DefaultRoleSetupImportService getDefaultRoleSetupImportService(DataSource dataSource) {
        return new DefaultRoleSetupImportService(dataSource);
    }
}
