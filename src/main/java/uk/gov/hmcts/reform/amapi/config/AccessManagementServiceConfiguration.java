package uk.gov.hmcts.reform.amapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.amlib.AccessManagementService;

import javax.sql.DataSource;

@Configuration
public class AccessManagementServiceConfiguration {

    @Bean
    public AccessManagementService getAccessManagementService(DataSource dataSource) {
        return new AccessManagementService(dataSource);
    }
}
