package integration.uk.gov.hmcts.reform.amlib.importer;

import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class ResourceDefinitionIntegrationTest extends IntegrationBaseTest {
    private static DefaultRoleSetupImportService service;

    @BeforeAll
    static void setUp() {
        service = new DefaultRoleSetupImportService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
    }

    @Test
    void shouldNotBeAbleToCreateResourceForServiceThatDoesNotExist() {
        assertThatExceptionOfType(UnableToExecuteStatementException.class).isThrownBy(() ->
            service.addResourceDefinition("fake service", RESOURCE_TYPE, RESOURCE_NAME))
            .withMessageContaining("(service_name)=(fake service) is not present in table \"services\"");
    }

    @Test
    void shouldAddNewResourceDefinitionIntoDatabaseWhenServiceNameExists() {
        service.addService(SERVICE_NAME);
        service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);

        assertThat(databaseHelper.countResources(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME)).isEqualTo(1);
    }

    @Test
    void shouldUpdateExistingEntryWhenDuplicateResourceDefinitionsAreAdded() {
        service.addService(SERVICE_NAME);
        service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);

        assertThat(databaseHelper.countResources(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME)).isEqualTo(1);
    }

    @Test
    void shouldDeleteResourceDefinitionFromTableWhenValueExists() {
        service.addService(SERVICE_NAME);
        service.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        service.deleteResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);

        assertThat(databaseHelper.countResources(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME)).isEqualTo(0);
    }
}
