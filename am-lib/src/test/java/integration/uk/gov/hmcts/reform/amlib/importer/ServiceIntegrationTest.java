package integration.uk.gov.hmcts.reform.amlib.importer;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.internal.models.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        Service service = databaseHelper.getService(serviceName);
        assertThat(service).isNotNull();
        assertThat(service.getLastUpdate()).isNotNull();
    }

    @Test
    void shouldUpdateExistingEntryWhenDuplicateServiceNameIsAdded() {
        String newDescription = "Different description";

        service.addService(serviceName);
        Service serviceDetails = databaseHelper.getService(serviceName);
        final LocalDateTime dateTime = serviceDetails.getLastUpdate();
        service.addService(serviceName, newDescription);

        serviceDetails = databaseHelper.getService(serviceName);
        assertThat(serviceDetails).isNotNull();
        assertThat(serviceDetails.getServiceDescription()).isEqualTo(newDescription);
        assertThat(serviceDetails.getLastUpdate()).isNotNull();
        assertThat(serviceDetails.getLastUpdate()).isNotEqualTo(dateTime);
    }

    @Test
    void shouldDeleteServiceFromTableWhenServiceIsPresent() {
        service.addService(serviceName);
        service.deleteService(serviceName);

        assertThat(databaseHelper.getService(serviceName)).isNull();
    }
}
