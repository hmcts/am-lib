package uk.gov.hmcts.reform.amapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportServiceImpl;

import javax.sql.DataSource;

@Configuration
public class DefaultRoleSetupImportServiceConfiguration {

    @Bean
    public DefaultRoleSetupImportServiceImpl getDefaultRoleSetupImportService(DataSource dataSource) {
        return new DefaultRoleSetupImportServiceImpl(dataSource);
    }
}
