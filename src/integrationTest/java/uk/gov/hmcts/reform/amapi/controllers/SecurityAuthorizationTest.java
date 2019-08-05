package uk.gov.hmcts.reform.amapi.controllers;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.WebApplicationContext;
//import uk.gov.hmcts.reform.amapi.client.S2sClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

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

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Rule
    public WireMockRule s2sService = new WireMockRule(8502);

    //@BeforeEach
    public String getS2sToken() {
        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sName);
        log.info("Configured S2S URL: " + s2sUrl);

        //return new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbV9hY2Nlc3NtZ210X2FwaSIsImV4cCI6MTU2NDc0MjcxN30.rIpRPLo3r"
            + "XGex6iZcq1kG1732h53P744Fq5NTTKqw33jlMotC7jDuOffyCnerXyQxjTuN93F2Iuu7gY3NJ99Pw";
    }

    @BeforeEach
    public void setUpWireMock() {

        s2sService.stubFor(get(urlEqualTo("/details"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("am")));
    }

    @AfterEach
    public abstract void tearDown();

}
