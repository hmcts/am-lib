package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createResource;

@SuppressWarnings({"LineLength","PMD.ExcessiveImports"})
public class FilterResourceWithSecurityClassificationTest extends PreconfiguredIntegrationBaseTest {


    private static String rootLevelAttribute;
    private static String rootLevelObject;
    private static String nestedAttribute;
    private static String rootLevelAttributeValue;
    private static String nestedAttributeValue;

    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);

    private String resourceId;
    private String accessorId;
    private String idamRoleWithRoleBaseAccess;
    private ResourceDefinition resourceDefinition;


    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();

        rootLevelAttribute = "/" + UUID.randomUUID().toString();
        rootLevelAttributeValue = UUID.randomUUID().toString();

        rootLevelObject = "/" + UUID.randomUUID().toString();
        nestedAttribute = UUID.randomUUID().toString();
        nestedAttributeValue = UUID.randomUUID().toString();

        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBaseAccess = UUID.randomUUID().toString(), IDAM, PUBLIC, ROLE_BASED);
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
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), PUBLIC);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelObject + "/" + nestedAttribute, ImmutableSet.of(READ), PUBLIC));

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition, createSecurityClassificationData()), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(createSecurityClassificationData())
                .build())
            .userSecurityClassification(PRIVATE)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), ImmutableSet.of(READ)))
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
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), PRIVATE);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelObject + "/" + nestedAttribute, ImmutableSet.of(READ), PUBLIC));

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition, createSecurityClassificationData()), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(createSecurityClassificationData())
                .build())
            .userSecurityClassification(PRIVATE)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    //This method checks if the User's security role has less privileges as of the requested attributes.
    //In this case, the filter method should return no attribute.
    @Test
    void whenRoleSecurityClassificationIsLessThanResourceSecurityClassificationShouldReturnNull() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, PUBLIC, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), PRIVATE);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelObject + "/" + nestedAttribute, ImmutableSet.of(READ), PUBLIC));

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition, createSecurityClassificationData()), attributePermissions);

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

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), RESTRICTED);
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), PRIVATE);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelObject + "/" + nestedAttribute, ImmutableSet.of(READ), PUBLIC));

        JsonNode data = JsonNodeFactory.instance.objectNode()
            .set(rootLevelObject.replace("/", ""), JsonNodeFactory.instance.objectNode()
                .put(nestedAttribute, nestedAttributeValue));

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition, createSecurityClassificationData()), attributePermissions);

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
                    JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    //If user has multiple roles, get the role which has highest privileges and do the filtering of permissions.
    //In this case, the filter method should return the attributes for which user is authorized.
    @Test
    void whenOneOfMultipleRoleSecurityClassificationsIsMoreThanAttributeSecurityClassificationShouldReturnAllAttributes() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, RESTRICTED, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(rootLevelAttribute), RESTRICTED);
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), PRIVATE);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelObject + "/" + nestedAttribute, ImmutableSet.of(READ), PUBLIC));

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition, createSecurityClassificationData()), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(createSecurityClassificationData())
                .build())
            .userSecurityClassification(RESTRICTED)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), ImmutableSet.of(READ)))
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
        attributePermissions.put(JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), PRIVATE);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedRestrictedAccess,
            resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedRestrictedAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedRestrictedAccess,
            resourceDefinition, rootLevelObject + "/" + nestedAttribute, ImmutableSet.of(READ), PUBLIC));

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedRestrictedAccess, idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition, createSecurityClassificationData()), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(createSecurityClassificationData())
                .build())
            .userSecurityClassification(RESTRICTED)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }


    @SuppressWarnings("PMD")
    @Test
    void whenNoRootSecurityClassificationShouldThrowException() {
        String idamRoleWithRoleBasedPrivateAccess = UUID.randomUUID().toString();
        importerService.addRole(idamRoleWithRoleBasedPrivateAccess, IDAM, RESTRICTED, ROLE_BASED);

        Map<JsonPointer, SecurityClassification> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf("/random"), PRIVATE);
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedPrivateAccess,
            resourceDefinition, rootLevelObject + "/" + nestedAttribute, ImmutableSet.of(READ), PUBLIC));

        Assertions.assertThrows(NoSuchElementException.class, () -> {
            service.filterResource(accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
                createResource(resourceId, resourceDefinition), attributePermissions);
        });
    }

    @Test
    void showOnlyVisibleAttributesForUseRoleBasedAccess() {
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBaseAccess,
            resourceDefinition, "",
            ImmutableSet.of(READ), PUBLIC));

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBaseAccess, resourceDefinition,
            rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBaseAccess, resourceDefinition,
            rootLevelObject + "/" + nestedAttribute, ImmutableSet.of(CREATE), PUBLIC));

        Map<JsonPointer, SecurityClassification> securityClassificationMap = new ConcurrentHashMap<>();
        securityClassificationMap.put(JsonPointer.valueOf(""), PUBLIC);
        securityClassificationMap.put(JsonPointer.valueOf(rootLevelAttribute), PUBLIC);
        securityClassificationMap.put(JsonPointer.valueOf(rootLevelObject), PUBLIC);
        securityClassificationMap.put(JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), PUBLIC);


        JsonNode data = JsonNodeFactory.instance.objectNode()
            .put(rootLevelAttribute.replace("/", ""), rootLevelAttributeValue);
        ((ObjectNode) data).putObject(rootLevelObject.replace("/", ""));//@Todo need to tested with 273

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBaseAccess), createResource(resourceId,
                resourceDefinition, createSecurityClassificationData()), securityClassificationMap);

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
                    JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), ImmutableSet.of(CREATE)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }


    @Test
    void whenUserWithExplicitAccessWithHighestSecurityClassificationThenReturnAllAttributes() {

        String idamRoleWithExplicitAccess;

        importerService.addRole(idamRoleWithExplicitAccess = UUID.randomUUID().toString(), IDAM, PUBLIC, EXPLICIT);

        service.grantExplicitResourceAccess(createGrantForWholeDocument(
            resourceId, accessorId, idamRoleWithExplicitAccess, resourceDefinition, ImmutableSet.of(READ)));

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithExplicitAccess, resourceDefinition,
                    rootLevelAttribute, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithExplicitAccess, resourceDefinition,
                    rootLevelObject, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithExplicitAccess, resourceDefinition,
            rootLevelObject + "/" + nestedAttribute, ImmutableSet.of(READ), PUBLIC));

        Map<JsonPointer, SecurityClassification> securityClassificationMap = new ConcurrentHashMap<>();
        securityClassificationMap.put(JsonPointer.valueOf(""), PUBLIC);
        securityClassificationMap.put(JsonPointer.valueOf(rootLevelAttribute), PUBLIC);
        securityClassificationMap.put(JsonPointer.valueOf(rootLevelObject), PUBLIC);
        securityClassificationMap.put(JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), PUBLIC);

        Map<JsonPointer, Set<Permission>> permissionMap = new ConcurrentHashMap<>();
        permissionMap.put(JsonPointer.valueOf(""), ImmutableSet.of(READ));
        permissionMap.put(JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ));
        permissionMap.put(JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ));
        permissionMap.put(JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), ImmutableSet.of(READ));

        service.grantExplicitResourceAccess(createGrant(
            resourceId, accessorId, idamRoleWithExplicitAccess, resourceDefinition, permissionMap));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithExplicitAccess), createResource(resourceId,
                resourceDefinition, createSecurityClassificationData()), securityClassificationMap);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(createSecurityClassificationData())
                .build())
            .userSecurityClassification(PUBLIC)
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelAttribute), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject), ImmutableSet.of(READ),
                    JsonPointer.valueOf(rootLevelObject + "/" + nestedAttribute), ImmutableSet.of(READ)))
                .accessType(EXPLICIT)
                .build())
            .relationships(ImmutableSet.of(idamRoleWithExplicitAccess))
            .build());
    }


    private JsonNode createSecurityClassificationData() {
        return JsonNodeFactory.instance.objectNode()
            .put(rootLevelAttribute.replace("/", ""), rootLevelAttributeValue)
            .set(rootLevelObject.replace("/", ""), JsonNodeFactory.instance.objectNode()
                .put(nestedAttribute, nestedAttributeValue));
    }
}
