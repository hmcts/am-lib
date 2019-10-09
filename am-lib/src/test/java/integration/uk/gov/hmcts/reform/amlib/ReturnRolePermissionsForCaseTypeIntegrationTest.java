package integration.uk.gov.hmcts.reform.amlib;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportServiceImpl;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.DefaultRolePermissions;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;
import uk.gov.hmcts.reform.amlib.models.RolePermissionsForCaseTypeEnvelope;
import uk.gov.hmcts.reform.amlib.service.DefaultRoleSetupImportService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.DELETE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ReturnRolePermissionsForCaseTypeIntegrationTest extends PreconfiguredIntegrationBaseTest {

    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportServiceImpl.class);
    private static String resourceType = "case";

    private String idamRoleWithRoleBasedAccess;
    private ResourceDefinition resourceDefinition;

    @BeforeEach
    void setUp() {
        importerService.addRole(idamRoleWithRoleBasedAccess = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        importerService.addResourceDefinition(resourceDefinition =
            createResourceDefinition(serviceName, resourceType, UUID.randomUUID().toString()));
    }

    @Test
    void whenNoRoleBasedAccessForCaseTypeThenReturnEmptyList() {
        ResourceDefinition resourceDefinition1;
        importerService.addResourceDefinition(resourceDefinition1 =
            createResourceDefinition(serviceName, resourceType, UUID.randomUUID().toString()));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition1, "", ImmutableSet.of(CREATE), PUBLIC));

        RolePermissionsForCaseTypeEnvelope result = importerService.getRolePermissionsForCaseType(
            resourceDefinition.getResourceName());

        assertThat(result).isEqualTo(RolePermissionsForCaseTypeEnvelope.builder()
            .caseTypeId(resourceDefinition.getResourceName())
            .defaultRolePermissions(ImmutableList.of())
            .build());
    }

    @Test
    void whenOnlyAttributeLevelRoleBasedAccessForCaseTypeThenReturnEmptyList() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, "/attribute", ImmutableSet.of(CREATE, DELETE), PUBLIC));

        RolePermissionsForCaseTypeEnvelope result = importerService.getRolePermissionsForCaseType(
            resourceDefinition.getResourceName());

        assertThat(result).isEqualTo(RolePermissionsForCaseTypeEnvelope.builder()
            .caseTypeId(resourceDefinition.getResourceName())
            .defaultRolePermissions(ImmutableList.of())
            .build());
    }

    @Test
    void whenOneRoleHasRootLevelRoleBasedAccessForCaseTypeThenReturnPermissionsForThatRole() {
        String idamRoleWithRoleBasedAccess1;
        ResourceDefinition resourceDefinition1;
        importerService.addRole(idamRoleWithRoleBasedAccess1 = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        importerService.addResourceDefinition(resourceDefinition1 =
            createResourceDefinition(serviceName, resourceType, UUID.randomUUID().toString()));

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, "", ImmutableSet.of(UPDATE, DELETE), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess1,
            resourceDefinition1, "", ImmutableSet.of(CREATE, READ), PUBLIC));

        RolePermissionsForCaseTypeEnvelope result = importerService.getRolePermissionsForCaseType(
            resourceDefinition.getResourceName());

        assertThat(result).isEqualTo(RolePermissionsForCaseTypeEnvelope.builder()
            .caseTypeId(resourceDefinition.getResourceName())
            .defaultRolePermissions(ImmutableList.of(DefaultRolePermissions.builder()
                .role(idamRoleWithRoleBasedAccess)
                .permissions(ImmutableSet.of(UPDATE, DELETE))
                .build()))
            .build());
    }

    @Test
    void whenMultipleRolesHaveRootLevelRoleBasedAccessForCaseTypeThenReturnPermissionsForAllRoles() {
        String idamRoleWithRoleBasedAccess1;
        String idamRoleWithRoleBasedAccess2;
        ResourceDefinition resourceDefinition1;
        importerService.addRole(idamRoleWithRoleBasedAccess1 = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        importerService.addRole(idamRoleWithRoleBasedAccess2 = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        importerService.addResourceDefinition(resourceDefinition1 =
            createResourceDefinition(serviceName, resourceType, UUID.randomUUID().toString()));

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, "", ImmutableSet.of(UPDATE, DELETE), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess1,
            resourceDefinition, "", ImmutableSet.of(CREATE, READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess2,
            resourceDefinition1, "", ImmutableSet.of(READ), PUBLIC));

        RolePermissionsForCaseTypeEnvelope result = importerService.getRolePermissionsForCaseType(
            resourceDefinition.getResourceName());

        assertThat(result).isEqualTo(RolePermissionsForCaseTypeEnvelope.builder()
            .caseTypeId(resourceDefinition.getResourceName())
            .defaultRolePermissions(orderedImmutableListOf(
                DefaultRolePermissions.builder()
                    .role(idamRoleWithRoleBasedAccess)
                    .permissions(ImmutableSet.of(UPDATE, DELETE))
                    .build(),
                DefaultRolePermissions.builder()
                    .role(idamRoleWithRoleBasedAccess1)
                    .permissions(ImmutableSet.of(CREATE, READ))
                    .build()
                )
            )
            .build());
    }

    @Test
    void whenOrderedListThenReturnOrderedList() {
        DefaultRolePermissions permissionsForRole = DefaultRolePermissions.builder()
            .role("A")
            .build();
        DefaultRolePermissions permissionsForRole1 = DefaultRolePermissions.builder()
            .role("B")
            .build();

        List<DefaultRolePermissions> result = orderedImmutableListOf(permissionsForRole, permissionsForRole1);

        assertThat(result).isEqualTo(ImmutableList.of(permissionsForRole, permissionsForRole1));
    }

    @Test
    void whenUnorderedListThenReturnOrderedList() {
        DefaultRolePermissions permissionsForRole = DefaultRolePermissions.builder()
            .role("B")
            .build();
        DefaultRolePermissions permissionsForRole1 = DefaultRolePermissions.builder()
            .role("A")
            .build();

        List<DefaultRolePermissions> result = orderedImmutableListOf(permissionsForRole, permissionsForRole1);

        assertThat(result).isEqualTo(ImmutableList.of(permissionsForRole1, permissionsForRole));
    }


    @Test
    public void whenRolePermissionsForCaseTypeListThenReturnResourceWithPermissions() {
        String idamRoleWithRoleBasedAccess1;
        ResourceDefinition resourceDefinition1;
        importerService.addRole(idamRoleWithRoleBasedAccess1 = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        String resourceName1;
        Set<Permission> permissions = ImmutableSet.of(UPDATE, DELETE);
        Set<Permission> permissions1 = ImmutableSet.of(CREATE, READ);
        importerService.addResourceDefinition(resourceDefinition1 =
            createResourceDefinition(serviceName, resourceType, resourceName1 = UUID.randomUUID().toString()));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, "", permissions, PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess1,
            resourceDefinition1, "", permissions1, PUBLIC));
        List caseTypeIds = ImmutableList.of(
            resourceDefinition.getResourceName(), resourceDefinition1.getResourceName());
        List<RolePermissionsForCaseTypeEnvelope> result = importerService.getRolePermissionsForCaseType(
            caseTypeIds);

        result.sort(comparing(RolePermissionsForCaseTypeEnvelope::getCaseTypeId));

        assertThat(result.size()).isEqualTo(2);

        String resourceName = resourceDefinition.getResourceName();
        List<RolePermissionsForCaseTypeEnvelope> expectedResourceAuditResult = ImmutableList.of(
            RolePermissionsForCaseTypeEnvelope.builder().caseTypeId(resourceName)
                .defaultRolePermissions(ImmutableList.of(DefaultRolePermissions.builder()
                    .role(idamRoleWithRoleBasedAccess).permissions(permissions).build())).build(),
            RolePermissionsForCaseTypeEnvelope.builder().caseTypeId(resourceName1)
                .defaultRolePermissions(ImmutableList.of(DefaultRolePermissions.builder()
                    .role(idamRoleWithRoleBasedAccess1).permissions(permissions1).build())).build());

        assertThat(result).isEqualTo(expectedResourceAuditResult.stream()
            .sorted(comparing(RolePermissionsForCaseTypeEnvelope::getCaseTypeId))
            .collect(toList()));
    }

    @Test
    void whenNoRoleBasedAccessForAnyCaseTypeInListThenReturnEmptyList() {
        String idamRoleWithRoleBasedAccess1;
        ResourceDefinition resourceDefinition1;
        importerService.addRole(idamRoleWithRoleBasedAccess1 = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        Set<Permission> permissions = ImmutableSet.of(UPDATE, DELETE);
        Set<Permission> permissions1 = ImmutableSet.of(CREATE, READ);
        importerService.addResourceDefinition(resourceDefinition1 =
            createResourceDefinition(serviceName, resourceType, UUID.randomUUID().toString()));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, "", permissions, PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess1,
            resourceDefinition1, "", permissions1, PUBLIC));

        List caseTypeIds = ImmutableList.of(
            UUID.randomUUID().toString(), UUID.randomUUID().toString());

        List<RolePermissionsForCaseTypeEnvelope> result = importerService.getRolePermissionsForCaseType(
            caseTypeIds);

        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void whenOneOfCaseTypeInListHasAttributeLevelPermissionsOnlyThenItIsNotReturned() {
        String idamRoleWithRoleBasedAccess1;
        ResourceDefinition resourceDefinition1;
        importerService.addRole(idamRoleWithRoleBasedAccess1 = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        String resourceName1;
        Set<Permission> permissions = ImmutableSet.of(UPDATE, DELETE);
        Set<Permission> permissions1 = ImmutableSet.of(CREATE, READ);
        importerService.addResourceDefinition(resourceDefinition1 =
            createResourceDefinition(serviceName, resourceType, resourceName1 = UUID.randomUUID().toString()));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, "/test", permissions, PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess1,
            resourceDefinition1, "", permissions1, PUBLIC));
        List caseTypeIds = ImmutableList.of(
            resourceDefinition.getResourceName(), resourceDefinition1.getResourceName());
        List<RolePermissionsForCaseTypeEnvelope> result = importerService.getRolePermissionsForCaseType(
            caseTypeIds);

        assertThat(result.size()).isEqualTo(1);

        List<RolePermissionsForCaseTypeEnvelope> expectedResourceAuditResult = ImmutableList.of(
            RolePermissionsForCaseTypeEnvelope.builder().caseTypeId(resourceName1)
                .defaultRolePermissions(ImmutableList.of(DefaultRolePermissions.builder()
                    .role(idamRoleWithRoleBasedAccess1).permissions(permissions1).build())).build());

        assertThat(result).isEqualTo(expectedResourceAuditResult);
    }

    @Test
    void whenOneOfCaseTypeInListHasMultipleRolesWithRootLevelAccessThenReturnPermissionsForAllRoles() {
        String idamRoleWithRoleBasedAccess1;
        String idamRoleWithRoleBasedAccess2;
        ResourceDefinition resourceDefinition1;
        importerService.addRole(idamRoleWithRoleBasedAccess1 = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        importerService.addRole(idamRoleWithRoleBasedAccess2 = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        String resourceName1;
        Set<Permission> permissions = ImmutableSet.of(UPDATE, DELETE);
        Set<Permission> permissions1 = ImmutableSet.of(CREATE, READ);
        importerService.addResourceDefinition(resourceDefinition1 =
            createResourceDefinition(serviceName, resourceType, resourceName1 = UUID.randomUUID().toString()));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, "", permissions, PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess2,
            resourceDefinition, "", permissions, PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess1,
            resourceDefinition1, "", permissions1, PUBLIC));
        List caseTypeIds = ImmutableList.of(
            resourceDefinition.getResourceName(), resourceDefinition1.getResourceName());
        List<RolePermissionsForCaseTypeEnvelope> result = importerService.getRolePermissionsForCaseType(
            caseTypeIds);

        result.forEach(rolePermissionsForCaseTypeEnvelope -> {
            rolePermissionsForCaseTypeEnvelope.getDefaultRolePermissions()
                .sort(comparing(DefaultRolePermissions::getRole));
        });
        result.sort(comparing(RolePermissionsForCaseTypeEnvelope::getCaseTypeId));

        List defaultRolePermissionsList = new ArrayList<>(Arrays.asList(DefaultRolePermissions.builder()
                .role(idamRoleWithRoleBasedAccess).permissions(permissions).build(),
            DefaultRolePermissions.builder()
                .role(idamRoleWithRoleBasedAccess2).permissions(permissions).build()));
        defaultRolePermissionsList.sort(comparing(DefaultRolePermissions::getRole));

        String resourceName = resourceDefinition.getResourceName();
        List<RolePermissionsForCaseTypeEnvelope> expectedResourceAuditResult = ImmutableList.of(
            RolePermissionsForCaseTypeEnvelope.builder().caseTypeId(resourceName)
                .defaultRolePermissions(defaultRolePermissionsList).build(),
            RolePermissionsForCaseTypeEnvelope.builder().caseTypeId(resourceName1)
                .defaultRolePermissions(ImmutableList.of(DefaultRolePermissions.builder()
                    .role(idamRoleWithRoleBasedAccess1).permissions(permissions1).build())).build());

        assertThat(result).isEqualTo(expectedResourceAuditResult.stream()
            .sorted(comparing(RolePermissionsForCaseTypeEnvelope::getCaseTypeId))
            .collect(toList()));
    }

    private List<DefaultRolePermissions> orderedImmutableListOf(DefaultRolePermissions... defaultRolePermissions) {
        Comparator<DefaultRolePermissions> compareByRole = comparing(DefaultRolePermissions::getRole,
            Comparator.naturalOrder());
        Arrays.sort(defaultRolePermissions, compareByRole);
        return Arrays.asList(defaultRolePermissions);
    }
}
