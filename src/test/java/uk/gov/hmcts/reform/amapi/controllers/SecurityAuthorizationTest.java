package uk.gov.hmcts.reform.amapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.amapi.client.S2sClient;

@ComponentScan("uk.gov.hmcts.reform.amapi")
@TestPropertySource("classpath:application-test.yml")
@Slf4j
public abstract class SecurityAuthorizationTest {

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;

    @Value("${s2s-secret}")
    protected String s2sSecret;


    //@BeforeEach
    public String getS2sToken() {
        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sName);
        log.info("Configured S2S URL: " + s2sUrl);

        return new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();
    }

    @AfterEach
    public abstract void tearDown();

}
