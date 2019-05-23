package uk.gov.hmcts.reform.amapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application implements CommandLineRunner {

    @Autowired
    private DefaultRoleSetupImportService importerService;

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        importerService.addService("cmc");
        importerService.addResourceDefinition(ResourceDefinition.builder()
            .serviceName("cmc")
            .resourceType("case")
            .resourceName("claim")
            .build());
        importerService.addRole("caseworker", RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.addRole("Role 1", RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
    }
}
