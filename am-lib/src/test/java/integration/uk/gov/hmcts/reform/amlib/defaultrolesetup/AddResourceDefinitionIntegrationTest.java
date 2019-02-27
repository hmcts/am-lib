package integration.uk.gov.hmcts.reform.amlib.defaultrolesetup;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class AddResourceDefinitionIntegrationTest extends IntegrationBaseTest {

    @Test
    void whenAddResourceDefinitionIsCalledAddNewEntryIntoDatabase() {
        defaultRoleService.addService(SERVICE_NAME, "");

        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        assertThat(countResources(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME)).isEqualTo(1);
    }

    @Test
    void whenCallIsMadeDuplicatingExistingResourceUpdatesExistingEntry() {
        defaultRoleService.addService(SERVICE_NAME, "");

        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);

        assertThat(countResources(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME)).isEqualTo(1);
    }

    @Test
    void canDeleteResourceDefinitionFromTable() {
        defaultRoleService.addService(SERVICE_NAME, "");

        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        defaultRoleService.deleteResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);

        assertThat(countResources(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME)).isEqualTo(0);
    }
}
