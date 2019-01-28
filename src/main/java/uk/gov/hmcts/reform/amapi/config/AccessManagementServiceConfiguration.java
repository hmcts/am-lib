package uk.gov.hmcts.reform.amapi.config;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.amlib.AccessManagementService;

@Configuration
public class AccessManagementServiceConfiguration {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    public AccessManagementService getAccessManagementService() {
        return new AccessManagementService(Jdbi.create(dbUrl, dbUsername, dbPassword));
    }
}
