package integration.uk.gov.hmcts.reform.amlib.importer;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class ServiceIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service;

    @BeforeAll
    static void setUp() {
        service = new DefaultRoleSetupImportService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
    }

    @Test
    void shouldPutNewRowInputIntoDatabaseWhenUniqueServiceNameIsGiven() {
        service.addService(SERVICE_NAME);

        assertThat(countServices(SERVICE_NAME)).hasSize(1);
    }

    @Test
    void shouldUpdateExistingEntryWhenDuplicateServiceNameIsAdded() {
        String newDescription = "Different description";

        service.addService(SERVICE_NAME);
        service.addService(SERVICE_NAME, newDescription);

        assertThat(countServices(SERVICE_NAME)).hasSize(1);
        assertThat(countServices(SERVICE_NAME).get(0)).containsValue(newDescription);
    }

    @Test
    void shouldDeleteServiceFromTableWhenServiceIsPresent() {
        service.addService(SERVICE_NAME);
        service.deleteService(SERVICE_NAME);

        assertThat(countServices(SERVICE_NAME)).isEmpty();
    }
}
