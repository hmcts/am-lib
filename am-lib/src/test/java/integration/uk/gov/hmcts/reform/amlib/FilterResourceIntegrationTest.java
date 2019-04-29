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
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.models.AccessEnvelope;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.OTHER_ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.RESOURCE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createResource;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "LineLength"})
class FilterResourceIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static final String PARENT_ATTRIBUTE = "/parent";
    private static final String CHILD_ATTRIBUTE = "/parent/child";
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
    private String resourceId;
    private String accessorId;
    private ResourceDefinition resourceDefinition;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();
        String resourceType = UUID.randomUUID().toString();
        importerService.addResourceDefinition(
            resourceDefinition = createResourceDefinition(serviceName, resourceType, RESOURCE_NAME));
    }

    @Test
    void whenRowExistsAndHasReadPermissionsShouldReturnEnvelopeWithData() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, accessorId, resourceDefinition, ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ROLE_NAMES, createResource(resourceId, resourceDefinition));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ROLE_NAMES)
            .build());
    }

    @Test
    void whenRowExistsAndDoesNotHaveReadPermissionsShouldReturnEnvelopeWithoutData() {
        service.grantExplicitResourceAccess(
            createGrantForWholeDocument(resourceId, accessorId, resourceDefinition, ImmutableSet.of(CREATE)));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ROLE_NAMES, createResource(resourceId, resourceDefinition));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(null)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(CREATE)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ROLE_NAMES)
            .build());
    }

    @Test
    void whenThereAreNoAccessRecordsShouldReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilteredResourceEnvelope result = service.filterResource(
            nonExistingUserId, ROLE_NAMES, createResource(nonExistingResourceId, resourceDefinition));

        assertThat(result).isNull();
    }

    @Test
    void whenNoExplicitAccessShouldUseRoleBasedAccess() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ROOT_ATTRIBUTE, ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ROLE_NAMES, createResource(resourceId, resourceDefinition));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(ROOT_ATTRIBUTE, ImmutableSet.of(READ)))
                .accessType(AccessType.ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenNoExplicitAccessAndRoleHasExplicitAccessTypeShouldReturnNull() {
        importerService.addRole(OTHER_ROLE_NAME, RoleType.RESOURCE, PUBLIC, EXPLICIT);

        FilteredResourceEnvelope result = service.filterResource(accessorId, ImmutableSet.of(OTHER_ROLE_NAME),
            createResource(resourceId, resourceDefinition));

        assertThat(result).isNull();
    }

    @Test
    void whenNoExplicitAccessAndMultipleRolesWhereOneHasExplicitAccessTypeShouldReturnOnlyRoleBasedPermissions() {
        importerService.addRole(OTHER_ROLE_NAME, RoleType.RESOURCE, PUBLIC, EXPLICIT);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ROOT_ATTRIBUTE, ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(OTHER_ROLE_NAME)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissionsForAttribute(
                JsonPointer.valueOf("/child"), ImmutableSet.of(READ), PUBLIC))
            .build());

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(ROLE_NAME, OTHER_ROLE_NAME), createResource(resourceId, resourceDefinition));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(ROOT_ATTRIBUTE, ImmutableSet.of(READ)))
                .accessType(AccessType.ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenListOfResourcesShouldReturnListFilteredResourceEnvelope() {
        importerService.addRole(ROLE_NAME, RoleType.RESOURCE, PUBLIC, AccessType.ROLE_BASED);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ROOT_ATTRIBUTE, ImmutableSet.of(READ)));

        List<Resource> resources = ImmutableList.of(
            createResource(resourceId, resourceDefinition),
            createResource(resourceId + 2, resourceDefinition));

        List<FilteredResourceEnvelope> result = service.filterResource(accessorId, ROLE_NAMES, resources);

        List<FilteredResourceEnvelope> expectedResult = ImmutableList.of(
            FilteredResourceEnvelope.builder()
                .resource(Resource.builder()
                    .id(resourceId)
                    .definition(resourceDefinition)
                    .data(DATA)
                    .build())
                .access(AccessEnvelope.builder()
                    .permissions(createPermissions("", ImmutableSet.of(READ)))
                    .accessType(AccessType.ROLE_BASED)
                    .build())
                .relationships(ImmutableSet.of())
                .build(),
            FilteredResourceEnvelope.builder()
                .resource(Resource.builder()
                    .id(resourceId + 2)
                    .definition(resourceDefinition)
                    .data(DATA)
                    .build())
                .access(AccessEnvelope.builder()
                    .permissions(createPermissions("", ImmutableSet.of(READ)))
                    .accessType(AccessType.ROLE_BASED)
                    .build())
                .relationships(ImmutableSet.of())
                .build()
        );
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void whenListOfResourcesButNoReadAccessShouldReturnListOfEnvelopesWithNullDataValues() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(resourceDefinition, ROOT_ATTRIBUTE, ImmutableSet.of(CREATE)));

        List<Resource> resources = ImmutableList.of(
            createResource(resourceId, resourceDefinition),
            createResource(resourceId + 2, resourceDefinition));

        List<FilteredResourceEnvelope> result = service.filterResource(accessorId, ROLE_NAMES, resources);

        List<FilteredResourceEnvelope> expectedResult = ImmutableList.of(
            FilteredResourceEnvelope.builder()
                .resource(Resource.builder()
                    .id(resourceId)
                    .definition(resourceDefinition)
                    .data(null)
                    .build())
                .access(AccessEnvelope.builder()
                    .permissions(createPermissions("", ImmutableSet.of(CREATE)))
                    .accessType(AccessType.ROLE_BASED)
                    .build())
                .relationships(ImmutableSet.of())
                .build(),
            FilteredResourceEnvelope.builder()
                .resource(Resource.builder()
                    .id(resourceId + 2)
                    .definition(resourceDefinition)
                    .data(null)
                    .build())
                .access(AccessEnvelope.builder()
                    .permissions(createPermissions("", ImmutableSet.of(CREATE)))
                    .accessType(AccessType.ROLE_BASED)
                    .build())
                .relationships(ImmutableSet.of())
                .build()
        );
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void whenEmptyListOfResourcesShouldReturnEmptyList() {
        List<Resource> resources = ImmutableList.of();

        List<FilteredResourceEnvelope> result = service.filterResource(accessorId, ROLE_NAMES, resources);

        assertThat(result).isEmpty();
    }

    @Test
    void whenExplicitAccessWithDifferentRelationshipSameAttributeAndDifferentPermissionsShouldMergePermissions() {
        importerService.addRole(OTHER_ROLE_NAME, IDAM, PUBLIC, EXPLICIT);

        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, ROLE_NAME, resourceDefinition,
            createPermissions(PARENT_ATTRIBUTE, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, OTHER_ROLE_NAME, resourceDefinition,
            createPermissions(PARENT_ATTRIBUTE, ImmutableSet.of(CREATE))));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ROLE_NAMES, createResource(resourceId, resourceDefinition));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(JsonNodeFactory.instance.objectNode())
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(PARENT_ATTRIBUTE), ImmutableSet.of(CREATE, READ)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ImmutableSet.of(OTHER_ROLE_NAME, ROLE_NAME))
            .build());
    }

    @Test
    void whenExplicitAccessWithSameRelationshipParentChildAttributesWithDiffPermissionsShouldNotMergePermissions() {
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, ROLE_NAME, resourceDefinition,
            createPermissions(PARENT_ATTRIBUTE, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, ROLE_NAME, resourceDefinition,
            createPermissions(CHILD_ATTRIBUTE, ImmutableSet.of(CREATE))));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ROLE_NAMES, createResource(resourceId, resourceDefinition));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(JsonNodeFactory.instance.objectNode())
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(PARENT_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(CHILD_ATTRIBUTE), ImmutableSet.of(CREATE)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ImmutableSet.of(ROLE_NAME))
            .build());
    }

    @Test
    void whenExplicitAccessWithDifferentRelationshipParentChildAttributeDiffPermissionsShouldMergePermissions() {
        importerService.addRole(OTHER_ROLE_NAME, IDAM, PUBLIC, EXPLICIT);
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, ROLE_NAME, resourceDefinition,
            createPermissions(PARENT_ATTRIBUTE, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, OTHER_ROLE_NAME, resourceDefinition,
            createPermissions(CHILD_ATTRIBUTE, ImmutableSet.of(CREATE))));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ROLE_NAMES, createResource(resourceId, resourceDefinition));

        assertThat(result).isEqualToComparingFieldByField(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(JsonNodeFactory.instance.objectNode())
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(PARENT_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(CHILD_ATTRIBUTE), ImmutableSet.of(CREATE, READ)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ImmutableSet.of(OTHER_ROLE_NAME, ROLE_NAME))
            .build());
    }
}
