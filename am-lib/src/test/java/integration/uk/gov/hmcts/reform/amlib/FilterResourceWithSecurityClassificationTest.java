package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Collections;
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
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createResource;

@SuppressWarnings({"LineLength", "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals", "PMD.ExcessiveImports"})
class FilterResourceWithSecurityClassificationTest extends PreconfiguredIntegrationBaseTest {

    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);

    private String resourceId;
    private String accessorId;
    private String idamRoleWithRoleBasedAccess;
    private ResourceDefinition resourceDefinition;

    private String rootLevelAttribute;
    private String rootLevelObject;
    private String nestedAttribute;
    private String rootLevelAttributeValue;
    private String nestedAttributeValue;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();

        rootLevelAttribute = "/" + UUID.randomUUID().toString();
        rootLevelAttributeValue = UUID.randomUUID().toString();
        rootLevelObject = "/" + UUID.randomUUID().toString();
        nestedAttribute = "/" + UUID.randomUUID().toString();
        nestedAttributeValue = UUID.randomUUID().toString();

        importerService.addRole(idamRoleWithRoleBasedAccess = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
        importerService.addResourceDefinition(resourceDefinition =
            createResourceDefinition(serviceName, UUID.randomUUID().toString(), UUID.randomUUID().toString()));
    }

    //This method checks if the User's security role has higher privileges than requested attributes.
    //In this case, the filter method should return all of the attributes.
    @Test
    void whenRoleSecurityClassificationIsMoreThanResourceSecurityClassificationShouldReturnAllAttributes() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PRIVATE, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PUBLIC);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), PUBLIC);
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + nestedAttribute), PUBLIC);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedPrivateAccess);
        JsonNode data = createSecurityClassificationData();

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess), createResource(resourceId,
                resourceDefinition, data), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PRIVATE)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    //This method checks if the User's security role has same privileges as of the requested input attributes.
    //In this case, the filter method should return all of the attributes.
    @Test
    void whenRoleSecurityClassificationMatchesResourceSecurityClassificationShouldReturnAllAttributes() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PRIVATE, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + nestedAttribute), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedPrivateAccess);
        JsonNode data = createSecurityClassificationData();

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition, data), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PRIVATE)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    //This method checks if the User's security role has less privileges as of the requested attributes.
    //In this case, the filter method should return no attribute.
    @Test
    void whenRoleSecurityClassificationIsLessThanResourceSecurityClassificationShouldReturnNull() {
        Map<JsonPointer, SecurityClassification> attributeSecurityClassifications = new ConcurrentHashMap<>();
        attributeSecurityClassifications.put(JsonPointer.valueOf(""), PRIVATE);
        attributeSecurityClassifications.put(JsonPointer.valueOf(rootLevelAttribute), PRIVATE);
        attributeSecurityClassifications.put(JsonPointer.valueOf(rootLevelObject), PRIVATE);
        attributeSecurityClassifications.put(JsonPointer.valueOf(rootLevelObject + nestedAttribute), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedAccess);
        JsonNode data = createSecurityClassificationData();

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId,
                resourceDefinition, data), attributeSecurityClassifications);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(null)
                .build())
            .userSecurityClassification(PUBLIC)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of())
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    //This method checks if few of the security classification attributes have higher privilege than the user security classification.
    //In this case, the filter method should return the attributes for which user is authorized.
    @Test
    void whenRoleSecurityClassificationIsLessThanAttributeSecurityClassificationShouldRemoveAttribute() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PRIVATE, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributeSecurityClassifications = new ConcurrentHashMap<>();
        attributeSecurityClassifications.put(JsonPointer.valueOf(""), PRIVATE);
        attributeSecurityClassifications.put(JsonPointer.valueOf(rootLevelAttribute), RESTRICTED);
        attributeSecurityClassifications.put(JsonPointer.valueOf(rootLevelObject + nestedAttribute), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedPrivateAccess);

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess), createResource(resourceId,
                resourceDefinition, createSecurityClassificationData()), attributeSecurityClassifications);

        JsonNode data = JsonNodeFactory.instance.objectNode()
            .set(rootLevelObject.replace("/",""), JsonNodeFactory.instance.objectNode()
                .put(nestedAttribute.replace("/",""), nestedAttributeValue));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PRIVATE)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    //If user has multiple roles, get the role which has highest privileges and do the filtering of permissions.
    //In this case, the filter method should return the attributes for which user is authorized.
    @Test
    void whenOneOfMultipleRoleSecurityClassificationsIsMoreThanAttributeSecurityClassificationShouldReturnAllAttributes() {
        String idamRoleWithRoleBasedRestrictedAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedRestrictedAccess, IDAM, RESTRICTED, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), RESTRICTED);
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + nestedAttribute), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedRestrictedAccess);
        JsonNode data = createSecurityClassificationData();

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedRestrictedAccess), createResource(resourceId,
                resourceDefinition, data), attributePermissions);


        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(RESTRICTED)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenUserHavingMultipleRolesSecurityClassificationThenShouldReturnHighestSecurityClassification() {
        String idamRoleWithRoleBasedRestrictedAccess = UUID.randomUUID().toString();
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PRIVATE, ROLE_BASED);
        importerService.addRole(idamRoleWithRoleBasedRestrictedAccess, IDAM, RESTRICTED, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), RESTRICTED);
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + nestedAttribute), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedRestrictedAccess);
        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedPrivateAccess);
        JsonNode data = createSecurityClassificationData();

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedRestrictedAccess, idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition, data), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(RESTRICTED)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @SuppressWarnings("PMD")
    @Test
    void whenNoRootSecurityClassificationShouldThrowException() {
        String idamRoleWithRoleBasedRestrictedAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedRestrictedAccess, IDAM, RESTRICTED, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf("/random"), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedRestrictedAccess);

        Assertions.assertThrows(NoSuchElementException.class, () -> {
            service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedRestrictedAccess),
                createResource(resourceId, resourceDefinition, createSecurityClassificationData()),
                attributePermissions);
        });
    }

    @Test
    void showOnlyVisibleAttributesForUseRoleBasedAccess() {
        Map<JsonPointer, SecurityClassification> securityClassifications = new ConcurrentHashMap<>();
        securityClassifications.put(JsonPointer.valueOf(""), PUBLIC);
        securityClassifications.put(JsonPointer.valueOf(rootLevelAttribute), PUBLIC);
        securityClassifications.put(JsonPointer.valueOf(rootLevelObject),PUBLIC);
        securityClassifications.put(JsonPointer.valueOf(rootLevelObject + nestedAttribute), PUBLIC);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, rootLevelObject, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, rootLevelObject + nestedAttribute, ImmutableSet.of(CREATE), PUBLIC));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId,
                resourceDefinition, createSecurityClassificationData()), securityClassifications);

        JsonNode data = JsonNodeFactory.instance.objectNode()
            .put(rootLevelAttribute.replace("/", ""), rootLevelAttributeValue)
            .set(rootLevelObject.replace("/", ""), JsonNodeFactory.instance.objectNode());

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PUBLIC)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(CREATE)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenNoSecurityClassificationThenShouldInheritFromClosestParent() {
        Map<JsonPointer, SecurityClassification> securityClassifications = new ConcurrentHashMap<>();
        securityClassifications.put(JsonPointer.valueOf(""), PRIVATE);
        securityClassifications.put(JsonPointer.valueOf(rootLevelAttribute), PRIVATE);
        securityClassifications.put(JsonPointer.valueOf(rootLevelObject), PUBLIC);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedAccess);

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId,
                resourceDefinition, createSecurityClassificationData()), securityClassifications);

        JsonNode data = JsonNodeFactory.instance.objectNode()
            .set(rootLevelObject.replace("/", ""), JsonNodeFactory.instance.objectNode()
                .put(nestedAttribute.replace("/", ""), nestedAttributeValue));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PUBLIC)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenNoSecurityClassificationAndNoParentSecurityClassificationThenShouldInheritFromRoot() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PRIVATE, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> securityClassifications =
            Collections.singletonMap(JsonPointer.valueOf(""), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedPrivateAccess);
        JsonNode data = createSecurityClassificationData();

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess), createResource(resourceId,
                resourceDefinition, data), securityClassifications);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PRIVATE)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenNoSecurityClassificationOrParentButChildHasSecurityClassificationThenShouldInheritFromRoot() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PRIVATE, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> securityClassifications = new ConcurrentHashMap<>();
        securityClassifications.put(JsonPointer.valueOf(""), PRIVATE);
        securityClassifications.put(JsonPointer.valueOf(rootLevelObject + nestedAttribute), RESTRICTED);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedPrivateAccess);

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess), createResource(resourceId,
                resourceDefinition, createSecurityClassificationData()), securityClassifications);

        JsonNode data = JsonNodeFactory.instance.objectNode()
            .put(rootLevelAttribute.replace("/", ""), rootLevelAttributeValue)
            .set(rootLevelObject.replace("/", ""), JsonNodeFactory.instance.objectNode());

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PRIVATE)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenMixtureOfPermissionsAndSecurityClassificationsThenFilteringShouldHandleBothIndependently() {
        Map<JsonPointer, SecurityClassification> securityClassifications = new ConcurrentHashMap<>();
        securityClassifications.put(JsonPointer.valueOf(""), PUBLIC);
        securityClassifications.put(JsonPointer.valueOf(rootLevelAttribute), PRIVATE);
        securityClassifications.put(JsonPointer.valueOf(rootLevelObject), PUBLIC);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, "", ImmutableSet.of(CREATE), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, rootLevelObject, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, rootLevelObject + nestedAttribute, ImmutableSet.of(CREATE), PUBLIC));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId,
                resourceDefinition, createSecurityClassificationData()), securityClassifications);

        JsonNode data = JsonNodeFactory.instance.objectNode()
            .set(rootLevelObject.replace("/", ""), JsonNodeFactory.instance.objectNode());

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PUBLIC)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(CREATE),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(CREATE)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenUserWithExplicitAccessWithHighestSecurityClassificationThenReturnAllAttributes() {
        String idamRoleWithExplicitAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithExplicitAccess, IDAM, PUBLIC, EXPLICIT);

        Map<JsonPointer, Set<Permission>> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), ImmutableSet.of(READ));
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ));
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ));
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(READ));

        service.grantExplicitResourceAccess(createGrant(
            resourceId, accessorId, idamRoleWithExplicitAccess, resourceDefinition, attributePermissions));

        Map<JsonPointer, SecurityClassification> attributeSecurityClassifications =
            Collections.singletonMap(JsonPointer.valueOf(""), PUBLIC);

        grantAllDefaultPermissionsForRole(idamRoleWithExplicitAccess);
        JsonNode data = createSecurityClassificationData();

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithExplicitAccess), createResource(resourceId,
                resourceDefinition, data), attributeSecurityClassifications);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PUBLIC)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ImmutableSet.of(idamRoleWithExplicitAccess))
            .build());
    }

    private void grantAllDefaultPermissionsForRole(String role) {
        importerService.grantDefaultPermission(
            createDefaultPermissionGrant(role, resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(
            createDefaultPermissionGrant(role, resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(
            createDefaultPermissionGrant(role, resourceDefinition, rootLevelObject, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(
            createDefaultPermissionGrant(role, resourceDefinition, rootLevelObject + nestedAttribute,
                ImmutableSet.of(READ), PUBLIC));
    }

    private JsonNode createSecurityClassificationData() {
        return JsonNodeFactory.instance.objectNode()
            .put(rootLevelAttribute.replace("/",""), rootLevelAttributeValue)
            .set(rootLevelObject.replace("/",""), JsonNodeFactory.instance.objectNode()
                .put(nestedAttribute.replace("/",""), nestedAttributeValue));
    }
}
