package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.AccessEnvelope;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.Resource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_DEFINITION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_TYPE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.SERVICE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createResource;

@SuppressWarnings("PMD.ExcessiveImports")
class FilterResourceIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
    private String resourceId;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    void whenRowExistsAndHasReadPermissionsShouldReturnEnvelopeWithData() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, READ_PERMISSION));

        FilteredResourceEnvelope result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resourceId(resourceId)
            .resourceDefinition(RESOURCE_DEFINITION)
            .data(DATA)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(JsonPointer.valueOf(""), READ_PERMISSION))
                .accessManagementType(AccessType.EXPLICIT)
                .build())
            .build());
    }

    @Test
    void whenRowExistsAndDoesNotHaveReadPermissionsShouldReturnEnvelopeWithoutData() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, CREATE_PERMISSION));

        FilteredResourceEnvelope result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resourceId(resourceId)
            .resourceDefinition(RESOURCE_DEFINITION)
            .data(null)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(JsonPointer.valueOf(""), CREATE_PERMISSION))
                .accessManagementType(AccessType.EXPLICIT)
                .build())
            .build());
    }

    @Test
    void whenThereAreNoAccessRecordsShouldReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilteredResourceEnvelope result = service.filterResource(
            nonExistingUserId, ROLE_NAMES, createResource(nonExistingResourceId));

        assertThat(result).isNull();
    }

    @Test
    void whenNoExplicitAccessShouldUseRoleBasedAccess() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, READ_PERMISSION));

        FilteredResourceEnvelope result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resourceId(resourceId)
            .resourceDefinition(RESOURCE_DEFINITION)
            .data(DATA)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(ROOT_ATTRIBUTE, READ_PERMISSION))
                .accessManagementType(AccessType.ROLE_BASED)
                .build())
            .build());
    }

    @Test
    void whenNoExplicitAccessAndRoleHasExplicitAccessTypeShouldReturnNull() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.EXPLICIT);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, READ_PERMISSION));

        FilteredResourceEnvelope result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isNull();
    }

    @Test
    void whenNoExplicitAccessAndMultipleRolesWhereOneHasExplicitAccessTypeShouldReturnOnlyRoleBasedPermissions() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.addRole(OTHER_ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.EXPLICIT);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, READ_PERMISSION));
        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(OTHER_ROLE_NAME)
            .resourceName(RESOURCE_NAME)
            .serviceName(SERVICE_NAME)
            .resourceType(RESOURCE_TYPE)
            .attributePermissions(createPermissionsForAttribute(JsonPointer.valueOf("/child"), READ_PERMISSION))
            .build());

        FilteredResourceEnvelope result = service.filterResource(
            ACCESSOR_ID, ImmutableSet.of(ROLE_NAME, OTHER_ROLE_NAME), createResource(resourceId));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resourceId(resourceId)
            .resourceDefinition(RESOURCE_DEFINITION)
            .data(DATA)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(ROOT_ATTRIBUTE, READ_PERMISSION))
                .accessManagementType(AccessType.ROLE_BASED)
                .build())
            .build());
    }

    @Test
    void whenListOfResourcesShouldReturnListFilterResourceResponse() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, READ_PERMISSION));

        List<Resource> resources = ImmutableList.of(
            createResource(resourceId),
            createResource(resourceId + "2"));

        List<FilteredResourceEnvelope> result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, resources);

        List<FilteredResourceEnvelope> expectedResult = ImmutableList.of(
            FilteredResourceEnvelope.builder()
                .resourceId(resourceId)
                .resourceDefinition(RESOURCE_DEFINITION)
                .data(DATA)
                .access(AccessEnvelope.builder()
                    .permissions(createPermissions("", READ_PERMISSION))
                    .accessManagementType(AccessType.ROLE_BASED)
                    .build())
                .build(),
            FilteredResourceEnvelope.builder()
                .resourceId(resourceId + "2")
                .resourceDefinition(RESOURCE_DEFINITION)
                .data(DATA)
                .access(AccessEnvelope.builder()
                    .permissions(createPermissions("", READ_PERMISSION))
                    .accessManagementType(AccessType.ROLE_BASED)
                    .build())
                .build()
        );

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void whenListOfResourcesButNoReadAccessShouldReturnListOfEnvelopesWithNullDataValues() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, CREATE_PERMISSION));

        List<Resource> resources = ImmutableList.of(
            createResource(resourceId),
            createResource(resourceId + "2"));

        List<FilteredResourceEnvelope> result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, resources);

        List<FilteredResourceEnvelope> expectedResult = ImmutableList.of(
            FilteredResourceEnvelope.builder()
                .resourceId(resourceId)
                .resourceDefinition(RESOURCE_DEFINITION)
                .data(null)
                .access(AccessEnvelope.builder()
                    .permissions(createPermissions("", CREATE_PERMISSION))
                    .accessManagementType(AccessType.ROLE_BASED)
                    .build())
                .build(),
            FilteredResourceEnvelope.builder()
                .resourceId(resourceId + "2")
                .resourceDefinition(RESOURCE_DEFINITION)
                .data(null)
                .access(AccessEnvelope.builder()
                    .permissions(createPermissions("", CREATE_PERMISSION))
                    .accessManagementType(AccessType.ROLE_BASED)
                    .build())
                .build()
        );

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void whenEmptyListOfResourcesShouldReturnEmptyList() {
        List<Resource> resources = ImmutableList.of();

        List<FilteredResourceEnvelope> result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, resources);

        assertThat(result).isEmpty();
    }
}
