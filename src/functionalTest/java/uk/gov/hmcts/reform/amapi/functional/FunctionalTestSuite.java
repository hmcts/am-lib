package uk.gov.hmcts.reform.amapi.functional;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.amapi.functional.client.AmApiClient;

@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
public class FunctionalTestSuite {


    @Value("${targetInstance}")
    protected String accessUrl;

    public AmApiClient amApiClient;

    @Before
    public void setUp() {
        log.info("Am api rest url::" + accessUrl);
        amApiClient = new AmApiClient(accessUrl);
    }

}
