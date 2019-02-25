package integration.uk.gov.hmcts.reform.amlib.DefaultRoleSetup;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;


class AddResourceDefinitionIntegrationTest extends IntegrationBaseTest {

    @Test
    void whenAddResourceDefinitionIsCalledAddNewEntryIntoDatabase() {
        defaultRoleService.addService(SERVICE_NAME, "");

        defaultRoleService.addResourceDefinition(SERVICE_NAME, "", "");
        assertThat(countResources(SERVICE_NAME, "", "")).isEqualTo(1);
    }

    @Test
    void whenCallIsMadeDuplicatingExistingResourceUpdatesExistingEntry() {
        defaultRoleService.addService(SERVICE_NAME, "");

        defaultRoleService.addResourceDefinition(SERVICE_NAME, "", "");
        defaultRoleService.addResourceDefinition(SERVICE_NAME, "", "");

        assertThat(countResources(SERVICE_NAME, "", "")).isEqualTo(1);
    }
}
