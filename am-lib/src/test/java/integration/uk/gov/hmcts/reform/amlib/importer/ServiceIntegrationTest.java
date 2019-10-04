package integration.uk.gov.hmcts.reform.amlib.importer;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportServiceImpl;
import uk.gov.hmcts.reform.amlib.internal.models.Service;
import uk.gov.hmcts.reform.amlib.service.DefaultRoleSetupImportService;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportServiceImpl.class);
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

    @Test
    void whenAuditDetailsThenShouldReturnAuditDetails() {
        String newDescription = "Different description";

        //Add Audit
        service.addService(serviceName);
        Service serviceDetails = databaseHelper.getService(serviceName);
        final Instant dateTime = serviceDetails.getLastUpdate();
        service.addService(serviceName, newDescription);
        assertThat(dateTime).isNotNull();

        //Update Audit
        serviceDetails = databaseHelper.getService(serviceName);
        assertThat(serviceDetails).isNotNull();
        assertThat(serviceDetails.getServiceDescription()).isEqualTo(newDescription);
        assertThat(serviceDetails.getLastUpdate()).isNotNull();
        assertThat(serviceDetails.getLastUpdate()).isNotEqualTo(dateTime);
    }
}
