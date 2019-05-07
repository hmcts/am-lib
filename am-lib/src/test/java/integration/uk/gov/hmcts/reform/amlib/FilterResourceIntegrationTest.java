package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.AccessEnvelope;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PRIVATE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.RESTRICTED;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createPermissions;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createResource;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "LineLength"})
class FilterResourceIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);
    private String resourceId;
    private String accessorId;
    private String idamRoleWithRoleBasedAccess;
    private String idamRoleWithExplicitAccess;
    private String rootLevelAttribute;
    private String nestedAttribute;
    private ResourceDefinition resourceDefinition;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();
        rootLevelAttribute = "/" + UUID.randomUUID().toString();
        nestedAttribute = rootLevelAttribute + "/" + UUID.randomUUID().toString();

        importerService.addRole(idamRoleWithRoleBasedAccess = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        importerService.addRole(idamRoleWithExplicitAccess = UUID.randomUUID().toString(), IDAM, PUBLIC, EXPLICIT);
        importerService.addResourceDefinition(resourceDefinition =
            createResourceDefinition(serviceName, UUID.randomUUID().toString(), UUID.randomUUID().toString()));
    }

    @Test
    void whenRowExistsAndHasReadPermissionsShouldReturnEnvelopeWithData() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, idamRoleWithRoleBasedAccess, resourceDefinition, ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId, resourceDefinition), getJsonPointerStringMap());

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
            .relationships(ImmutableSet.of(idamRoleWithRoleBasedAccess))
            .build());
    }

    @Test
    void whenRowExistsAndDoesNotHaveReadPermissionsShouldReturnEnvelopeWithoutData() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, idamRoleWithRoleBasedAccess, resourceDefinition, ImmutableSet.of(CREATE)));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId, resourceDefinition), getJsonPointerStringMap());

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
            .relationships(ImmutableSet.of(idamRoleWithRoleBasedAccess))
            .build());
    }

    @Test
    void whenThereAreNoAccessRecordsShouldReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilteredResourceEnvelope result = service.filterResource(
            nonExistingUserId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(nonExistingResourceId, resourceDefinition), getJsonPointerStringMap());

        assertThat(result).isNull();
    }

    @Test
    void whenNoExplicitAccessShouldUseRoleBasedAccess() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess, resourceDefinition, "", ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId, resourceDefinition), getJsonPointerStringMap());

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(ROOT_ATTRIBUTE, ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenNoExplicitAccessAndRoleHasExplicitAccessTypeShouldReturnNull() {
        FilteredResourceEnvelope result = service.filterResource(accessorId, ImmutableSet.of(idamRoleWithExplicitAccess),
            createResource(resourceId, resourceDefinition), getJsonPointerStringMap());

        assertThat(result).isNull();
    }

    @Test
    void whenNoExplicitAccessAndMultipleRolesWhereOneHasExplicitAccessTypeShouldReturnOnlyRoleBasedPermissions() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess, resourceDefinition, "", ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(idamRoleWithExplicitAccess)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissionsForAttribute(
                JsonPointer.valueOf("/xyz"), ImmutableSet.of(READ), PUBLIC))
            .build());

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess, idamRoleWithExplicitAccess), createResource(resourceId, resourceDefinition), getJsonPointerStringMap());

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(ROOT_ATTRIBUTE, ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenListOfResourcesShouldReturnListFilteredResourceEnvelope() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess, resourceDefinition, "", ImmutableSet.of(READ)));

        List<Resource> resources = ImmutableList.of(
            createResource(resourceId, resourceDefinition),
            createResource(resourceId + 2, resourceDefinition));

        List<FilteredResourceEnvelope> result =
            service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), resources, getJsonPointerStringMap());

        List<FilteredResourceEnvelope> expectedResult = ImmutableList.of(
            FilteredResourceEnvelope.builder()
                .resource(Resource.builder()
                    .id(resourceId)
                    .definition(resourceDefinition)
                    .data(DATA)
                    .build())
                .access(AccessEnvelope.builder()
                    .permissions(createPermissions("", ImmutableSet.of(READ)))
                    .accessType(ROLE_BASED)
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
                    .accessType(ROLE_BASED)
                    .build())
                .relationships(ImmutableSet.of())
                .build()
        );
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void whenListOfResourcesButNoReadAccessShouldReturnListOfEnvelopesWithNullDataValues() {
        importerService.grantDefaultPermission(
            createDefaultPermissionGrant(idamRoleWithRoleBasedAccess, resourceDefinition, "", ImmutableSet.of(CREATE)));

        List<Resource> resources = ImmutableList.of(
            createResource(resourceId, resourceDefinition),
            createResource(resourceId + 2, resourceDefinition));

        List<FilteredResourceEnvelope> result =
            service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), resources, getJsonPointerStringMap());

        List<FilteredResourceEnvelope> expectedResult = ImmutableList.of(
            FilteredResourceEnvelope.builder()
                .resource(Resource.builder()
                    .id(resourceId)
                    .definition(resourceDefinition)
                    .data(null)
                    .build())
                .access(AccessEnvelope.builder()
                    .permissions(createPermissions("", ImmutableSet.of(CREATE)))
                    .accessType(ROLE_BASED)
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
                    .accessType(ROLE_BASED)
                    .build())
                .relationships(ImmutableSet.of())
                .build()
        );
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void whenEmptyListOfResourcesShouldReturnEmptyList() {
        List<Resource> resources = ImmutableList.of();

        List<FilteredResourceEnvelope> result =
            service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), resources, getJsonPointerStringMap());

        assertThat(result).isEmpty();
    }

    @Test
    void whenExplicitAccessWithDifferentRelationshipSameAttributeAndDifferentPermissionsShouldMergePermissions() {
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithRoleBasedAccess, resourceDefinition,
            createPermissions(rootLevelAttribute, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithExplicitAccess, resourceDefinition,
            createPermissions(rootLevelAttribute, ImmutableSet.of(CREATE))));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId, resourceDefinition),
            createJsonPointerStringMap(rootLevelAttribute, nestedAttribute));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(JsonNodeFactory.instance.objectNode())
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(CREATE, READ)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ImmutableSet.of(idamRoleWithExplicitAccess, idamRoleWithRoleBasedAccess))
            .build());
    }

    @Test
    void whenExplicitAccessWithSameRelationshipParentChildAttributesWithDiffPermissionsShouldNotMergePermissions() {
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithRoleBasedAccess, resourceDefinition,
            createPermissions(rootLevelAttribute, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithRoleBasedAccess, resourceDefinition,
            createPermissions(nestedAttribute, ImmutableSet.of(CREATE))));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId, resourceDefinition),
            createJsonPointerStringMap(rootLevelAttribute, nestedAttribute));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(JsonNodeFactory.instance.objectNode())
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(nestedAttribute), ImmutableSet.of(CREATE)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ImmutableSet.of(idamRoleWithRoleBasedAccess))
            .build());
    }

    @Test
    void whenExplicitAccessWithDifferentRelationshipParentChildAttributeDiffPermissionsShouldMergePermissions() {
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithRoleBasedAccess, resourceDefinition,
            createPermissions(rootLevelAttribute, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithExplicitAccess, resourceDefinition,
            createPermissions(nestedAttribute, ImmutableSet.of(CREATE))));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId, resourceDefinition),
            createJsonPointerStringMap(rootLevelAttribute, nestedAttribute));

        assertThat(result).isEqualToComparingFieldByField(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(JsonNodeFactory.instance.objectNode())
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(nestedAttribute), ImmutableSet.of(CREATE, READ)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ImmutableSet.of(idamRoleWithExplicitAccess, idamRoleWithRoleBasedAccess))
            .build());
    }

    private DefaultPermissionGrant createDefaultPermissionGrant(String roleName,
                                                                ResourceDefinition resourceDefinition,
                                                                String attribute,
                                                                Set<Permission> permissions) {
        return DefaultPermissionGrant.builder()
            .roleName(roleName)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissionsForAttribute(JsonPointer.valueOf(attribute), permissions, PUBLIC))
            .build();
    }

    @Test
    void whenRoleSecurityClassificationIsMoreThanResourceSecurityClassificationShouldReturnAllAttributes() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PRIVATE, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PUBLIC);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), PUBLIC);
        attributePermissions.put(JsonPointer.valueOf(nestedAttribute), PUBLIC);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, nestedAttribute, ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenRoleSecurityClassificationMatchesResourceSecurityClassificationShouldReturnAllAttributes() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PRIVATE, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PUBLIC);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(nestedAttribute), PUBLIC);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, nestedAttribute, ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenRoleSecurityClassificationIsLessThanResourceSecurityClassificationShouldReturnNull() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PUBLIC, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(nestedAttribute), PRIVATE);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, nestedAttribute, ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(null)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of())
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenRoleSecurityClassificationIsLessThanAttributeSecurityClassificationShouldRemoveAttribute() {
        //Permissions are looking fine, data needs to be updated.
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PRIVATE, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), RESTRICTED);
        attributePermissions.put(JsonPointer.valueOf(nestedAttribute), PRIVATE);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, nestedAttribute, ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenOneOfMultipleRoleSecurityClassificationsIsMoreThanAttributeSecurityClassificationShouldReturnAllAttributes() {
        //Permissions are looking fine, data needs to be updated.
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, RESTRICTED, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), RESTRICTED);
        attributePermissions.put(JsonPointer.valueOf(nestedAttribute), PRIVATE);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, nestedAttribute, ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @SuppressWarnings("PMD")
    @Test
    void whenNoRootSecurityClassificationShouldThrowException() {
        //Permissions are looking fine, data needs to be updated.
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, RESTRICTED, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf("/random"), PRIVATE);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, nestedAttribute, ImmutableSet.of(READ)));

        Assertions.assertThrows(NoSuchElementException.class, () -> {
            service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
                createResource(resourceId, resourceDefinition), attributePermissions);
        });
    }

    private Map<JsonPointer, SecurityClassification> getJsonPointerStringMap() {
        Map<JsonPointer, SecurityClassification> map = new ConcurrentHashMap<>();
        map.put(JsonPointer.valueOf(""), PUBLIC);
        map.put(JsonPointer.valueOf(rootLevelAttribute), PUBLIC);
        map.put(JsonPointer.valueOf(nestedAttribute), PUBLIC);
        return map;
    }

    private Map<JsonPointer, SecurityClassification> createJsonPointerStringMap(String... args) {
        Map<JsonPointer, SecurityClassification> map = new ConcurrentHashMap<>();
        map.put(JsonPointer.valueOf(""), PUBLIC);
        for (String attribute : args) {
            map.put(JsonPointer.valueOf(attribute), PUBLIC);
        }
        return map;
    }
}
