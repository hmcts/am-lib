package uk.gov.hmcts.reform.amapi.functional;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.amapi.functional.client.AmApiClient;


@TestPropertySource("classpath:application-functional.yaml")
public class FunctionalTestSuite {


    @Value("${accessInstance}")
    protected String accessUrl;

    public AmApiClient amApiClient;

    @Before
    public void setUp() {
        amApiClient = new AmApiClient(accessUrl);
    }

}
