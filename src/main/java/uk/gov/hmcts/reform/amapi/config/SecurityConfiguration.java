package uk.gov.hmcts.reform.amapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.AuthCheckerServiceOnlyFilter;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


    private final RequestAuthorizer<Service> serviceRequestAuthorizer;
    private final AuthenticationManager      authenticationManager;

    public SecurityConfiguration(
        RequestAuthorizer<Service> serviceRequestAuthorizer,
        AuthenticationManager authenticationManager) {
        super();
        this.serviceRequestAuthorizer = serviceRequestAuthorizer;
        this.authenticationManager = authenticationManager;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        AuthCheckerServiceOnlyFilter authCheckerServiceOnlyFilter = new AuthCheckerServiceOnlyFilter(
            serviceRequestAuthorizer);

        authCheckerServiceOnlyFilter.setAuthenticationManager(authenticationManager);

        http.requestMatchers()
            .antMatchers(HttpMethod.POST,"/api/**")
            .antMatchers(HttpMethod.GET,"/api/**")
            .antMatchers(HttpMethod.DELETE,"/api/**")
            .and()
            .addFilter(authCheckerServiceOnlyFilter)
            .csrf().disable()
            .authorizeRequests()
            .anyRequest().authenticated();


    }
}
