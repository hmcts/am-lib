package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessManagementType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;
import uk.gov.hmcts.reform.amlib.models.Resource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CHILD_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.PARENT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createResource;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class FilterResourceIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
    private String resourceId;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        importerService.addRole(ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessManagementType.EXPLICIT);
    }

    @Test
    void whenRowExistsAndHasReadPermissionsShouldReturnEnvelopeWithData() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, READ_PERMISSION));

        FilterResourceResponse result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .relationships(ImmutableSet.of(ROLE_NAME))
            .data(DATA)
            .permissions(ImmutableMap.of(JsonPointer.valueOf(""), READ_PERMISSION))
            .build());
    }

    @Test
    void whenRowExistsAndDoesNotHaveReadPermissionsShouldReturnEnvelopeWithoutData() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, CREATE_PERMISSION));

        FilterResourceResponse result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .relationships(ImmutableSet.of(ROLE_NAME))
            .data(null)
            .permissions(ImmutableMap.of(JsonPointer.valueOf(""), CREATE_PERMISSION))
            .build());
    }

    @Test
    void whenThereAreNoAccessRecordsShouldReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilterResourceResponse result = service.filterResource(
            nonExistingUserId, ROLE_NAMES, createResource(nonExistingResourceId));

        assertThat(result).isNull();
    }

    @Test
    void whenNoExplicitAccessShouldUseRoleBasedAccess() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC,
            AccessManagementType.ROLE_BASED);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, READ_PERMISSION));

        FilterResourceResponse result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .relationships(ImmutableSet.of())
            .data(DATA)
            .permissions(ImmutableMap.of(ROOT_ATTRIBUTE, READ_PERMISSION))
            .build());
    }

    @Test
    void whenNoExplicitAccessAndRoleHasExplicitAccessTypeShouldReturnNull() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, READ_PERMISSION));

        FilterResourceResponse result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isNull();
    }

    @Test
    void whenListOfResourcesShouldReturnListFilterResourceResponse() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC,
            AccessManagementType.ROLE_BASED);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, READ_PERMISSION));

        List<Resource> resources = ImmutableList.of(
            createResource(resourceId),
            createResource(resourceId + "2"));

        List<FilterResourceResponse> result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, resources);

        List<FilterResourceResponse> expectedResult = ImmutableList.of(
            FilterResourceResponse.builder()
                .resourceId(resourceId)
                .relationships(ImmutableSet.of())
                .data(DATA)
                .permissions(createPermissions("", READ_PERMISSION))
                .build(),
            FilterResourceResponse.builder()
                .resourceId(resourceId + "2")
                .relationships(ImmutableSet.of())
                .data(DATA)
                .permissions(createPermissions("", READ_PERMISSION))
                .build());

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void whenListOfResourcesButNoReadAccessShouldReturnListOfEnvelopesWithNullDataValues() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC,
            AccessManagementType.ROLE_BASED);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, CREATE_PERMISSION));

        List<Resource> resources = ImmutableList.of(
            createResource(resourceId),
            createResource(resourceId + "2"));

        List<FilterResourceResponse> result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, resources);

        List<FilterResourceResponse> expectedResult = ImmutableList.of(
            FilterResourceResponse.builder()
                .resourceId(resourceId)
                .relationships(ImmutableSet.of())
                .data(null)
                .permissions(createPermissions("", CREATE_PERMISSION))
                .build(),
            FilterResourceResponse.builder()
                .resourceId(resourceId + "2")
                .relationships(ImmutableSet.of())
                .data(null)
                .permissions(createPermissions("", CREATE_PERMISSION))
                .build());

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void whenEmptyListOfResourcesShouldReturnEmptyList() {
        List<Resource> resources = ImmutableList.of();

        List<FilterResourceResponse> result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, resources);

        assertThat(result).isEmpty();
    }

    @Test
    void whenExplicitAccessWithDifferentRelationshipSameAttributeAndDifferentPermissionsShouldMergePermissions() {
        importerService.addRole(
            OTHER_ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessManagementType.EXPLICIT);

        service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_ID, ROLE_NAME,
            createPermissions(PARENT_ATTRIBUTE, READ_PERMISSION)));
        service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_ID, OTHER_ROLE_NAME,
            createPermissions(PARENT_ATTRIBUTE, CREATE_PERMISSION)));

        FilterResourceResponse result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .relationships(ImmutableSet.of(OTHER_ROLE_NAME, ROLE_NAME))
            .data(JsonNodeFactory.instance.objectNode())
            .permissions(ImmutableMap.of(
                JsonPointer.valueOf(PARENT_ATTRIBUTE), ImmutableSet.of(CREATE, READ)))
            .build());
    }

    @Test
    void whenExplicitAccessWithSameRelationshipParentChildAttributesWithDiffPermissionsShouldNotMergePermissions() {
        service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_ID, ROLE_NAME,
            createPermissions(PARENT_ATTRIBUTE, READ_PERMISSION)));
        service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_ID, ROLE_NAME,
            createPermissions(CHILD_ATTRIBUTE, CREATE_PERMISSION)));

        FilterResourceResponse result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .relationships(ImmutableSet.of(ROLE_NAME))
            .data(JsonNodeFactory.instance.objectNode())
            .permissions(ImmutableMap.of(
                JsonPointer.valueOf(CHILD_ATTRIBUTE), ImmutableSet.of(CREATE),
                JsonPointer.valueOf(PARENT_ATTRIBUTE), ImmutableSet.of(READ)))
            .build());
    }

    @Test
    void whenExplicitAccessWithDifferentRelationshipParentChildAttributeDiffPermissionsShouldMergePermissions() {
        importerService.addRole(
            OTHER_ROLE_NAME, RoleType.IDAM, SecurityClassification.PUBLIC, AccessManagementType.EXPLICIT);

        service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_ID, ROLE_NAME,
            createPermissions(PARENT_ATTRIBUTE, READ_PERMISSION)));
        service.grantExplicitResourceAccess(createGrant(resourceId, ACCESSOR_ID, OTHER_ROLE_NAME,
            createPermissions(CHILD_ATTRIBUTE, CREATE_PERMISSION)));

        FilterResourceResponse result = service.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .relationships(ImmutableSet.of(OTHER_ROLE_NAME, ROLE_NAME))
            .data(JsonNodeFactory.instance.objectNode())
            .permissions(ImmutableMap.of(
                JsonPointer.valueOf(CHILD_ATTRIBUTE), ImmutableSet.of(CREATE, READ),
                JsonPointer.valueOf(PARENT_ATTRIBUTE), ImmutableSet.of(READ)))
            .build());
    }
}
