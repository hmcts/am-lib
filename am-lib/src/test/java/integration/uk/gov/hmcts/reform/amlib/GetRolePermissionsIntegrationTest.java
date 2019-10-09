package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportServiceImpl;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.internal.models.query.AttributeData;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.Pair;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.models.RolePermissions;
import uk.gov.hmcts.reform.amlib.service.DefaultRoleSetupImportService;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.DELETE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PRIVATE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.RESTRICTED;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_INSERTION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CALLING_SERVICE_NAME_FOR_UPDATES;

@SuppressWarnings({"PMD.TooManyMethods","PMD.ExcessiveImports"})
class GetRolePermissionsIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportServiceImpl.class);

    private ResourceDefinition resourceDefinition;
    private String resourceType;
    private String resourceName;
    private String roleName;

    @BeforeEach
    void setUp() {
        roleName = UUID.randomUUID().toString();
        resourceType = UUID.randomUUID().toString();
        resourceName = UUID.randomUUID().toString();

        importerService.addResourceDefinition(resourceDefinition =
            createResourceDefinition(serviceName, resourceType, resourceName));
    }

    @Test
    void whenGettingRolePermissionsShouldReturnPermissionsAndSecurityClassificationsForSpecifiedRole() {
        Map.Entry<Set<Permission>, SecurityClassification> publicReadPermission =
            new Pair<>(ImmutableSet.of(READ), PUBLIC);

        Map.Entry<Set<Permission>, SecurityClassification> privateReadPermission =
            new Pair<>(ImmutableSet.of(READ), PRIVATE);

        Map.Entry<Set<Permission>, SecurityClassification> restrictedAllPermission =
            new Pair<>(ImmutableSet.of(CREATE, READ, UPDATE, DELETE), RESTRICTED);

        addRoleWithSecurityClassification(roleName, RESTRICTED);
        grantDefaultPermissionForRole(roleName, ImmutableMap.of(
            JsonPointer.valueOf("/child"), publicReadPermission,
            JsonPointer.valueOf("/parent/age"), privateReadPermission,
            JsonPointer.valueOf("/parent/address"), restrictedAllPermission
        ));

        RolePermissions rolePermissions =
            service.getRolePermissions(resourceDefinition, roleName);

        assertThat(rolePermissions).isEqualTo(RolePermissions.builder()
            .permissions(ImmutableMap.of(
                JsonPointer.valueOf("/child"), ImmutableSet.of(READ),
                JsonPointer.valueOf("/parent/age"), ImmutableSet.of(READ),
                JsonPointer.valueOf("/parent/address"), ImmutableSet.of(CREATE, READ, UPDATE, DELETE)
            ))
            .securityClassifications(ImmutableMap.of(
                JsonPointer.valueOf("/child"), PUBLIC,
                JsonPointer.valueOf("/parent/age"), PRIVATE,
                JsonPointer.valueOf("/parent/address"), RESTRICTED
            ))
            .roleAccessType(ROLE_BASED)
            .roleSecurityClassification(RESTRICTED)
            .build());
    }

    @Test
    void whenGettingRolePermissionsShouldRemoveAttributesWithHigherSecurityClassification() {
        Map.Entry<Set<Permission>, SecurityClassification> publicReadPermission =
            new Pair<>(ImmutableSet.of(READ), PUBLIC);
        Map.Entry<Set<Permission>, SecurityClassification> restrictedReadPermission =
            new Pair<>(ImmutableSet.of(READ), RESTRICTED);

        addRoleWithSecurityClassification(roleName, PUBLIC);
        grantDefaultPermissionForRole(roleName, ImmutableMap.of(
            JsonPointer.valueOf(""), publicReadPermission,
            JsonPointer.valueOf("/orders"), restrictedReadPermission
        ));

        RolePermissions rolePermissions = service.getRolePermissions(resourceDefinition, roleName);

        assertThat(rolePermissions).isEqualTo(RolePermissions.builder()
            .permissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
            .securityClassifications(ImmutableMap.of(JsonPointer.valueOf(""), PUBLIC))
            .roleAccessType(ROLE_BASED)
            .roleSecurityClassification(PUBLIC)
            .build());
    }

    @Test
    void whenInsufficientRolePermissionsShouldReturnNull() {
        Map.Entry<Set<Permission>, SecurityClassification> publicReadPermission =
            new Pair<>(ImmutableSet.of(READ), RESTRICTED);

        addRoleWithSecurityClassification(roleName, PUBLIC);
        grantDefaultPermissionForRole(roleName, ImmutableMap.of(JsonPointer.valueOf("/payment"), publicReadPermission));

        RolePermissions rolePermissions =
            service.getRolePermissions(resourceDefinition, roleName);

        assertThat(rolePermissions).isNull();
    }

    @Test
    void shouldReturnNullWhenServiceNameDoesNotExist() {
        RolePermissions rolePermissions = service.getRolePermissions(
            createResourceDefinition("Unknown Service", resourceType, resourceName), roleName);

        assertThat(rolePermissions).isNull();
    }

    @Test
    void shouldReturnNullWhenResourceTypeDoesNotExist() {
        RolePermissions rolePermissions = service.getRolePermissions(
            createResourceDefinition(serviceName, "Unknown Resource Type", resourceName), roleName);

        assertThat(rolePermissions).isNull();
    }

    @Test
    void shouldReturnNullWhenResourceNameDoesNotExist() {
        RolePermissions rolePermissions = service.getRolePermissions(
            createResourceDefinition(serviceName, resourceType, "Unknown Resource Name"), roleName);

        assertThat(rolePermissions).isNull();
    }

    @Test
    void shouldReturnNullWhenDefaultRoleNameDoesNotExist() {
        RolePermissions rolePermissions = service.getRolePermissions(resourceDefinition, "Unknown Role");

        assertThat(rolePermissions).isNull();
    }

    @Test
    void whenAuditLogsForRolePermissionAttributesThenShouldReturnsAuditDetails() {

        Map.Entry<Set<Permission>, SecurityClassification> publicReadPermission =
            new Pair<>(ImmutableSet.of(READ), PUBLIC);


        //Add Audit
        addRoleWithSecurityClassification(roleName, PUBLIC);
        grantDefaultPermissionForRoleWithAudit(roleName, ImmutableMap.of(
            JsonPointer.valueOf(""), publicReadPermission), CALLING_SERVICE_NAME_FOR_INSERTION
        );

        RolePermissions rolePermissions = service.getRolePermissions(resourceDefinition, roleName);
        rolePermissions.getPermissions();
        Set<SecurityClassification> securityClassificationSet = new HashSet<>();
        securityClassificationSet.add(PUBLIC);
        List<AttributeData> attributeData = databaseHelper.getAttributeDataForResource(resourceDefinition,
            roleName, securityClassificationSet);
        assertThat(attributeData).isNotNull();
        Instant dateTime = attributeData.get(0).getLastUpdate();
        assertThat(dateTime).isNotNull();
        assertThat(attributeData.get(0).getCallingServiceName())
            .isEqualTo(CALLING_SERVICE_NAME_FOR_INSERTION);

        //Update Audit
        addRoleWithSecurityClassification(roleName, PUBLIC);
        grantDefaultPermissionForRoleWithAudit(roleName, ImmutableMap.of(
            JsonPointer.valueOf(""), publicReadPermission), CALLING_SERVICE_NAME_FOR_UPDATES
        );
        attributeData = databaseHelper.getAttributeDataForResource(resourceDefinition, roleName,
            securityClassificationSet);
        assertThat(attributeData).isNotNull();
        assertThat(attributeData.get(0).getLastUpdate()).isNotNull();
        assertThat(attributeData.get(0).getCallingServiceName())
            .isEqualTo(CALLING_SERVICE_NAME_FOR_UPDATES);
        assertThat(dateTime).isNotEqualTo(attributeData.get(0).getLastUpdate());
    }

    private void addRoleWithSecurityClassification(String roleName, SecurityClassification securityClassification) {
        importerService.addRole(roleName, IDAM, securityClassification, ROLE_BASED);
    }

    @SuppressWarnings("LineLength")
    private void grantDefaultPermissionForRole(String roleName,
                                               Map<JsonPointer, Map.Entry<Set<Permission>,
                                               SecurityClassification>> attributePermissions) {
        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(attributePermissions)
            .build());
    }

    private void grantDefaultPermissionForRoleWithAudit(String roleName,
                                                        Map<JsonPointer, Map.Entry<Set<Permission>,
                                                        SecurityClassification>> attributePermissions,
                                                        String callingServiceName) {
        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(attributePermissions)
            .callingServiceName(callingServiceName)
            .build());
    }
}
