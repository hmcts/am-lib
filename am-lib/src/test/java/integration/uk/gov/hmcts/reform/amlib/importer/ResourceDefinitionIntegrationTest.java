package integration.uk.gov.hmcts.reform.amlib.importer;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.exceptions.PersistenceException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;

class ResourceDefinitionIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportService.class);
    private String serviceName;

    @BeforeEach
    void setUp() {
        serviceName = UUID.randomUUID().toString();
    }

    @Test
    void shouldNotBeAbleToCreateResourceForServiceThatDoesNotExist() {
        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
            service.addResourceDefinition(createResourceDefinition("fake service", RESOURCE_TYPE, RESOURCE_NAME)))
            .withMessageContaining("(service_name)=(fake service) is not present in table \"services\"");
    }

    @Test
    void shouldAddNewResourceDefinitionIntoDatabaseWhenServiceNameExists() {
        service.addService(serviceName);
        service.addResourceDefinition(createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME));

        assertThat(databaseHelper.getResourcesDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME)).isNotNull();
    }

    @Test
    void shouldUpdateExistingEntryWhenDuplicateResourceDefinitionsAreAdded() {
        service.addService(serviceName);
        service.addResourceDefinition(createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME));
        service.addResourceDefinition(createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME));

        assertThat(databaseHelper.getResourcesDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME)).isNotNull();
    }

    @Test
    void shouldDeleteResourceDefinitionFromTableWhenValueExists() {
        service.addService(serviceName);
        service.addResourceDefinition(createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME));
        service.deleteResourceDefinition(createResourceDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME));

        assertThat(databaseHelper.getResourcesDefinition(serviceName, RESOURCE_TYPE, RESOURCE_NAME)).isNull();
    }
}
