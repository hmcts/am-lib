package integration.uk.gov.hmcts.reform.amlib.importer;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.internal.models.Service;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;

class ServiceIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportService.class);
    private String serviceName;

    @BeforeEach
    void setUp() {
        serviceName = UUID.randomUUID().toString();
    }


    @Test
    void shouldPutNewRowInputIntoDatabaseWhenUniqueServiceNameIsGiven() {
        service.addService(serviceName);

        assertThat(databaseHelper.getService(serviceName)).isNotNull();
    }

    @Test
    void shouldUpdateExistingEntryWhenDuplicateServiceNameIsAdded() {
        String newDescription = "Different description";

        service.addService(serviceName);
        service.addService(serviceName, newDescription);
        //@Tod do removed
        service.getService(serviceName);
        service.addRole("any", IDAM, PUBLIC, ROLE_BASED);
        service.getRole("any");
        service.getExplicitAccessRecord();

        Service service = databaseHelper.getService(serviceName);
        assertThat(service).isNotNull();
        assertThat(service.getServiceDescription()).isEqualTo(newDescription);
    }

    @Test
    void shouldDeleteServiceFromTableWhenServiceIsPresent() {
        service.addService(serviceName);
        service.deleteService(serviceName);

        assertThat(databaseHelper.getService(serviceName)).isNull();
    }
}
