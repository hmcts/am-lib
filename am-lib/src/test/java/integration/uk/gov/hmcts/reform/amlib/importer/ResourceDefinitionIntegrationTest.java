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

class ResourceDefinitionIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service = initService(DefaultRoleSetupImportService.class);
    private String serviceName;
    private String resourceType;
    private String resourceName;

    @BeforeEach
    void setUp() {
        serviceName = UUID.randomUUID().toString();
        resourceType = UUID.randomUUID().toString();
        resourceName = UUID.randomUUID().toString();
    }

    @Test
    void shouldNotBeAbleToCreateResourceForServiceThatDoesNotExist() {
        assertThatExceptionOfType(PersistenceException.class).isThrownBy(() ->
            service.addResourceDefinition(createResourceDefinition("fake service", resourceType, resourceName)))
            .withMessageContaining("(service_name)=(fake service) is not present in table \"services\"");
    }

    @Test
    void shouldAddNewResourceDefinitionIntoDatabaseWhenServiceNameExists() {
        service.addService(serviceName);
        service.addResourceDefinition(createResourceDefinition(serviceName, resourceType, resourceName));

        assertThat(databaseHelper.getResourcesDefinition(serviceName, resourceType, resourceName)).isNotNull();
    }

    @Test
    void shouldUpdateExistingEntryWhenDuplicateResourceDefinitionsAreAdded() {
        service.addService(serviceName);
        service.addResourceDefinition(createResourceDefinition(serviceName, resourceType, resourceName));
        service.addResourceDefinition(createResourceDefinition(serviceName, resourceType, resourceName));

        assertThat(databaseHelper.getResourcesDefinition(serviceName, resourceType, resourceName)).isNotNull();
    }

    @Test
    void shouldDeleteResourceDefinitionFromTableWhenValueExists() {
        service.addService(serviceName);
        service.addResourceDefinition(createResourceDefinition(serviceName, resourceType, resourceName));
        service.deleteResourceDefinition(createResourceDefinition(serviceName, resourceType, resourceName));

        assertThat(databaseHelper.getResourcesDefinition(serviceName, resourceType, resourceName)).isNull();
    }
}
