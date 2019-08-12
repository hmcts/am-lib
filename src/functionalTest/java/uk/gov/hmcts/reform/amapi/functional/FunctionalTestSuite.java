package uk.gov.hmcts.reform.amapi.functional;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.amapi.functional.conf.SerenityBeanConfiguration;
import uk.gov.hmcts.reform.amapi.functional.client.AmApiClient;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.RESOURCE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;

@TestPropertySource("classpath:application-functional.yaml")
@Import(SerenityBeanConfiguration.class)
@Slf4j
public class FunctionalTestSuite {


    @Autowired
    private DefaultRoleSetupImportService importerService;

    @Value("${targetInstance}")
    protected String accessUrl;

    public AmApiClient amApiClient;

    @Before
    public void setUp() {
        log.info("Am api rest url::" + accessUrl);
        amApiClient = new AmApiClient(accessUrl);
        importerService.addService("cmc-test");

        ResourceDefinition resourceDefinition = ResourceDefinition.builder()
            .serviceName("cmc-test").resourceType("case-test").resourceName("claim-test").build();
        importerService.addResourceDefinition(resourceDefinition);
        importerService.addRole("caseworker-test", RESOURCE, PUBLIC, EXPLICIT);
    }

}
