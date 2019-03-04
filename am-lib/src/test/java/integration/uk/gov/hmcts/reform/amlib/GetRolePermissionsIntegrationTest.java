package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import integration.uk.gov.hmcts.reform.amlib.base.IntegrationBaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.grantMultipleResources;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;

class GetRolePermissionsIntegrationTest extends IntegrationBaseTest {
    private static AccessManagementService ams;
    private static DefaultRoleSetupImportService defaultRoleService;

    @BeforeAll
    static void setUp() {
        ams = new AccessManagementService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
        defaultRoleService = new DefaultRoleSetupImportService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
    }

    @BeforeEach
    void roleSetUp() {

        defaultRoleService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        defaultRoleService.addService(SERVICE_NAME);
        defaultRoleService.addResourceDefinition(SERVICE_NAME, RESOURCE_TYPE, RESOURCE_NAME);
        defaultRoleService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(ROLE_NAME)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .resourceName(RESOURCE_NAME)
            .attributePermissions(grantMultipleResources())
            .build());


    }

    @Test
    void returnListOfPermissionsForRoleName() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAMES);

        assertThat(accessRecord).containsEntry(JsonPointer.valueOf("/test"), READ_PERMISSION);
        assertThat(accessRecord).containsEntry(JsonPointer.valueOf("/test2"), READ_PERMISSION);
        assertThat(accessRecord).containsEntry(JsonPointer.valueOf("/testCreate"), CREATE_PERMISSION);


        assertThat(accessRecord).hasSize(3);
    }

    @Test
    void shouldReturnNullWhenServiceNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions("Service 2",
            RESOURCE_TYPE, RESOURCE_NAME, ROLE_NAMES);
        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenResourceTypeDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            "No Resource Type ", RESOURCE_NAME, ROLE_NAMES);
        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenResourceNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            RESOURCE_TYPE, "No Resource Name", ROLE_NAMES);
        assertThat(accessRecord).isNull();
    }

    @Test
    void shouldReturnNullWhenDefaultRoleNameDoesNotExist() {
        Map<JsonPointer, Set<Permission>> accessRecord = ams.getRolePermissions(SERVICE_NAME,
            RESOURCE_TYPE, RESOURCE_NAME, Stream.of("citizen").collect(toSet()));
        assertThat(accessRecord).isNull();
    }
}
