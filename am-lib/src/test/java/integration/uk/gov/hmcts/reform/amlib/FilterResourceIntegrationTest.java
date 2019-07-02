package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.models.AccessEnvelope;
import uk.gov.hmcts.reform.amlib.models.DefaultPermissionGrant;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.EXPLICIT;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createPermissionsForAttribute;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
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
    private String idamRoleWithRoleBaseAccess;
    private String idamRoleWithExplicitAccess;
    private String rootLevelAttribute;
    private String nestedAttribute;
    private String rootLevelObject;
    private String rootLevelObjectNestedAttribute;
    private ResourceDefinition resourceDefinition;
    private static String rootLevelAttributeValue;
    private static String nestedAttributeValue;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();
        rootLevelAttribute = "/" + UUID.randomUUID().toString();
        nestedAttribute = rootLevelAttribute + "/" + UUID.randomUUID().toString();

        rootLevelObject = "/" + UUID.randomUUID().toString();
        rootLevelObjectNestedAttribute = UUID.randomUUID().toString();

        rootLevelAttributeValue = UUID.randomUUID().toString();
        nestedAttributeValue = UUID.randomUUID().toString();

        importerService.addRole(idamRoleWithRoleBaseAccess = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        importerService.addRole(idamRoleWithExplicitAccess = UUID.randomUUID().toString(), IDAM, PUBLIC, EXPLICIT);
        importerService.addResourceDefinition(resourceDefinition =
            createResourceDefinition(serviceName, UUID.randomUUID().toString(), UUID.randomUUID().toString()));
    }

    @Test
    void whenRowExistsAndHasReadPermissionsShouldReturnEnvelopeWithData() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, idamRoleWithRoleBaseAccess, resourceDefinition, ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess),
            createResource(resourceId, resourceDefinition, createData()), null);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(createData())
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ImmutableSet.of(idamRoleWithRoleBaseAccess))
            .build());
    }

    @Test
    void whenSameResourceWithMultipleResourceDefinitionExistsShouldReturnEnvelopeBasedOnResourceType() {

        ResourceDefinition firstResourceDefinition =
            createResourceDefinition(serviceName, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        importerService.addResourceDefinition(firstResourceDefinition);

        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, idamRoleWithRoleBaseAccess, firstResourceDefinition, ImmutableSet.of(READ)));

        ResourceDefinition secondResourceDefinition =
            createResourceDefinition(serviceName, UUID.randomUUID().toString(), UUID.randomUUID().toString());
        importerService.addResourceDefinition(secondResourceDefinition);

        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, idamRoleWithRoleBaseAccess, secondResourceDefinition, ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess),
            createResource(resourceId, secondResourceDefinition, createData()), null);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(secondResourceDefinition)
                .data(createData())
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(JsonPointer.valueOf(""), ImmutableSet.of(READ)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ImmutableSet.of(idamRoleWithRoleBaseAccess))
            .build());
    }

    @Test
    void whenRowExistsAndDoesNotHaveReadPermissionsShouldReturnEnvelopeWithoutData() {
        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, idamRoleWithRoleBaseAccess, resourceDefinition, ImmutableSet.of(CREATE)));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess), createResource(resourceId, resourceDefinition),
            null);

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
            .relationships(ImmutableSet.of(idamRoleWithRoleBaseAccess))
            .build());
    }

    @Test
    void whenThereAreNoAccessRecordsShouldReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilteredResourceEnvelope result = service.filterResource(
            nonExistingUserId, ImmutableSet.of(idamRoleWithRoleBaseAccess),
            createResource(nonExistingResourceId, resourceDefinition),null);

        assertThat(result).isNull();
    }

    @Test
    void whenNoExplicitAccessShouldUseRoleBasedAccess() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBaseAccess,
            resourceDefinition, "", ImmutableSet.of(READ)));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess),
            createResource(resourceId, resourceDefinition, createData()), null);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(createData())
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
        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithExplicitAccess), createResource(resourceId, resourceDefinition),
            null);

        assertThat(result).isNull();
    }

    @Test
    void whenNoExplicitAccessAndMultipleRolesWhereOneHasExplicitAccessTypeShouldReturnOnlyRoleBasedPermissions() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBaseAccess,
            resourceDefinition, "", ImmutableSet.of(READ)));
        importerService.grantDefaultPermission(DefaultPermissionGrant.builder()
            .roleName(idamRoleWithExplicitAccess)
            .resourceDefinition(resourceDefinition)
            .attributePermissions(createPermissionsForAttribute(
                JsonPointer.valueOf("/" + UUID.randomUUID().toString()), ImmutableSet.of(READ), PUBLIC))
            .build());

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess, idamRoleWithExplicitAccess),
            createResource(resourceId, resourceDefinition, createData()), null);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(createData())
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
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBaseAccess,
            resourceDefinition, "", ImmutableSet.of(READ)));

        List<Resource> resources = ImmutableList.of(
            createResource(resourceId, resourceDefinition, createData()),
            createResource(resourceId + 2, resourceDefinition, createData()));

        List<FilteredResourceEnvelope> result = service.filterResources(accessorId,
            ImmutableSet.of(idamRoleWithRoleBaseAccess), resources, null);

        List<FilteredResourceEnvelope> expectedResult = ImmutableList.of(
            FilteredResourceEnvelope.builder()
                .resource(Resource.builder()
                    .id(resourceId)
                    .definition(resourceDefinition)
                    .data(createData())
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
                    .data(createData())
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
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBaseAccess,
            resourceDefinition, "", ImmutableSet.of(CREATE)));

        List<Resource> resources = ImmutableList.of(
            createResource(resourceId, resourceDefinition),
            createResource(resourceId + 2, resourceDefinition));

        List<FilteredResourceEnvelope> result =
            service.filterResources(accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess), resources,
                null);

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
            service.filterResources(accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess), resources,
                null);

        assertThat(result).isEmpty();
    }

    @Test
    void whenExplicitAccessWithDifferentRelationshipSameAttributeAndDifferentPermissionsShouldMergePermissions() {
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithRoleBaseAccess,
            resourceDefinition, createPermissions(rootLevelAttribute, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithExplicitAccess,
            resourceDefinition, createPermissions(rootLevelAttribute, ImmutableSet.of(CREATE))));

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBaseAccess), createResource(resourceId, resourceDefinition),
            null);

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
            .relationships(ImmutableSet.of(idamRoleWithExplicitAccess, idamRoleWithRoleBaseAccess))
            .build());
    }

    @Test
    void whenExplicitAccessWithSameRelationshipParentChildAttributesWithDiffPermissionsShouldNotMergePermissions() {
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithRoleBaseAccess,
            resourceDefinition, createPermissions(rootLevelAttribute, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithRoleBaseAccess,
            resourceDefinition, createPermissions(nestedAttribute, ImmutableSet.of(CREATE))));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess), createResource(resourceId, resourceDefinition),
            null);

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
            .relationships(ImmutableSet.of(idamRoleWithRoleBaseAccess))
            .build());
    }

    @Test
    void whenExplicitAccessWithDifferentRelationshipParentChildAttributeDiffPermissionsShouldMergePermissions() {
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithRoleBaseAccess,
            resourceDefinition, createPermissions(rootLevelAttribute, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithExplicitAccess,
            resourceDefinition, createPermissions(nestedAttribute, ImmutableSet.of(CREATE))));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess), createResource(resourceId, resourceDefinition),
            null);

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
            .relationships(ImmutableSet.of(idamRoleWithExplicitAccess, idamRoleWithRoleBaseAccess))
            .build());
    }

    @Test
    void whenExplicitAccessWithNullRelationshipShouldReturnEnvelope() {

        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, null, resourceDefinition,
            createPermissions(rootLevelAttribute, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, null, resourceDefinition,
            createPermissions(nestedAttribute, ImmutableSet.of(CREATE,READ))));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess), createResource(resourceId, resourceDefinition),
            null);


        assertThat(result).isEqualToComparingFieldByField(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(JsonNodeFactory.instance.objectNode())
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(nestedAttribute), ImmutableSet.of(CREATE,READ)))
                .accessType(EXPLICIT)
                .build())
            .relationships(Collections.emptySet())
            .build());
    }

    @Test
    void mergeResultEnvelopeWhenExplicitAccessWithNullRelationshipAndNotnullRelationShip() {

        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, null, resourceDefinition,
            createPermissions(rootLevelAttribute, ImmutableSet.of(UPDATE))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithExplicitAccess, resourceDefinition,
            createPermissions(rootLevelAttribute, ImmutableSet.of(READ))));
        service.grantExplicitResourceAccess(createGrant(resourceId, accessorId, idamRoleWithExplicitAccess, resourceDefinition,
            createPermissions(nestedAttribute, ImmutableSet.of(CREATE))));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess), createResource(resourceId, resourceDefinition),
            null);

        Set<String> exceptedRelationship = new HashSet();
        exceptedRelationship.add(idamRoleWithExplicitAccess);

        assertThat(result).isEqualToComparingFieldByField(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(JsonNodeFactory.instance.objectNode())
                .build())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ,UPDATE),
                    JsonPointer.valueOf(nestedAttribute), ImmutableSet.of(UPDATE, CREATE)))
                .accessType(EXPLICIT)
                .build())
            .relationships(exceptedRelationship)
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

    private JsonNode createData() {
        return JsonNodeFactory.instance.objectNode()
            .put(rootLevelAttribute.replace("/", ""), rootLevelAttributeValue)
            .set(rootLevelObject.replace("/", ""), JsonNodeFactory.instance.objectNode()
                .put(rootLevelObjectNestedAttribute, nestedAttributeValue));
    }
}

