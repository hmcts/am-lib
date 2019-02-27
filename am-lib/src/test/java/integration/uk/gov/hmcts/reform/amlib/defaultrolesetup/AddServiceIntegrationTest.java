package integration.uk.gov.hmcts.reform.amlib.defaultrolesetup;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class AddServiceIntegrationTest extends IntegrationBaseTest {

    @Test
    void whenAddServiceRowInputIntoDatabase() {
        defaultRoleService.addService(SERVICE_NAME, "");
        assertThat(countServices(SERVICE_NAME)).isEqualTo(1);
    }

    @Test
    void whenDuplicateServiceUpdatesExistingEntry() {
        defaultRoleService.addService(SERVICE_NAME, "");
        defaultRoleService.addService(SERVICE_NAME, "");
        assertThat(countServices(SERVICE_NAME)).isEqualTo(1);
    }

    @Test
    void canDeleteServiceFromTable() {
        defaultRoleService.addService(SERVICE_NAME, "");

        //Resource must be deleted before a service can be deleted.
        defaultRoleService.deleteResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);

        defaultRoleService.deleteService(SERVICE_NAME);

        assertThat(countServices(SERVICE_NAME)).isEqualTo(0);
    }
}
