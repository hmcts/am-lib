package integration.uk.gov.hmcts.reform.amlib.importer;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.buildResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class ResourceDefinitionIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportService.class);

    @Test
    void shouldNotBeAbleToCreateResourceForServiceThatDoesNotExist() {
        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
            service.addResourceDefinition(buildResourceDefinition("fake service", RESOURCE_TYPE, RESOURCE_NAME)))
            .withMessageContaining("(service_name)=(fake service) is not present in table \"services\"");
    }

    @Test
    void shouldAddNewResourceDefinitionIntoDatabaseWhenServiceNameExists() {
        service.addService(SERVICE_NAME);
        service.addResourceDefinition(buildResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME));

        assertThat(databaseHelper.getResourcesDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME)).isNotNull();
    }

    @Test
    void shouldUpdateExistingEntryWhenDuplicateResourceDefinitionsAreAdded() {
        service.addService(SERVICE_NAME);
        service.addResourceDefinition(buildResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME));
        service.addResourceDefinition(buildResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME));

        assertThat(databaseHelper.getResourcesDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME)).isNotNull();
    }

    @Test
    void shouldDeleteResourceDefinitionFromTableWhenValueExists() {
        service.addService(SERVICE_NAME);
        service.addResourceDefinition(buildResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME));
        service.deleteResourceDefinition(buildResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME));

        assertThat(databaseHelper.getResourcesDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME)).isNull();
    }
}
