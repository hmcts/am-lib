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
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.AccessEnvelope;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.ResourceDefinition;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.enums.AccessType.ROLE_BASED;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.RoleType.IDAM;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PRIVATE;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.PUBLIC;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.RESTRICTED;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createResourceDefinition;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createResource;

@SuppressWarnings({"LineLength", "PMD.TooManyMethods"})
class FilterResourceWithSecurityClassificationTest extends PreconfiguredIntegrationBaseTest {

    private static final String NAME_ATTRIBUTE = "/name";
    private static final String ADDRESS_ATTRIBUTE = "/address";
    private static final String CITY_ATTRIBUTE = "/address/city";
    private static AccessManagementService service = initService(AccessManagementService.class);
    private static DefaultRoleSetupImportService importerService = initService(DefaultRoleSetupImportService.class);

    private String resourceId;
    private String accessorId;
    private String idamRoleWithRoleBasedAccess;
    private ResourceDefinition resourceDefinition;


    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID().toString();
        accessorId = UUID.randomUUID().toString();
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
        attributePermissions.put(JsonPointer.valueOf(NAME_ATTRIBUTE), PUBLIC);
        attributePermissions.put(JsonPointer.valueOf(CITY_ATTRIBUTE), PUBLIC);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedPrivateAccess);

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .userSecurityClassification(PRIVATE.toString())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(NAME_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(ADDRESS_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(CITY_ATTRIBUTE), ImmutableSet.of(READ)))
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
        attributePermissions.put(JsonPointer.valueOf(NAME_ATTRIBUTE), PRIVATE);
        attributePermissions.put(JsonPointer.valueOf(CITY_ATTRIBUTE), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedPrivateAccess);

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .userSecurityClassification(PRIVATE.toString())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(NAME_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(ADDRESS_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(CITY_ATTRIBUTE), ImmutableSet.of(READ)))
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
        attributeSecurityClassifications.put(JsonPointer.valueOf(NAME_ATTRIBUTE), PRIVATE);
        attributeSecurityClassifications.put(JsonPointer.valueOf(ADDRESS_ATTRIBUTE), PRIVATE);
        attributeSecurityClassifications.put(JsonPointer.valueOf(CITY_ATTRIBUTE), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedAccess);

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedAccess),
            createResource(resourceId, resourceDefinition), attributeSecurityClassifications);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(null)
                .build())
            .userSecurityClassification(PUBLIC.toString())
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
        attributeSecurityClassifications.put(JsonPointer.valueOf(NAME_ATTRIBUTE), RESTRICTED);
        attributeSecurityClassifications.put(JsonPointer.valueOf(CITY_ATTRIBUTE), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedPrivateAccess);

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess), createResource(resourceId, resourceDefinition),
            attributeSecurityClassifications);

        JsonNode data = JsonNodeFactory.instance.objectNode()
            .set("address", JsonNodeFactory.instance.objectNode()
                .put("city", "London"));

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PRIVATE.toString())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(ADDRESS_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(CITY_ATTRIBUTE), ImmutableSet.of(READ)))
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
        attributePermissions.put(JsonPointer.valueOf(NAME_ATTRIBUTE), RESTRICTED);
        attributePermissions.put(JsonPointer.valueOf(CITY_ATTRIBUTE), PRIVATE);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedRestrictedAccess);

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedRestrictedAccess),
            createResource(resourceId, resourceDefinition), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .userSecurityClassification(RESTRICTED.toString())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(NAME_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(ADDRESS_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(CITY_ATTRIBUTE), ImmutableSet.of(READ)))
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
        attributePermissions.put(JsonPointer.valueOf(NAME_ATTRIBUTE), RESTRICTED);
        attributePermissions.put(JsonPointer.valueOf(CITY_ATTRIBUTE), PRIVATE);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedRestrictedAccess,
            resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedRestrictedAccess,
            resourceDefinition, NAME_ATTRIBUTE, ImmutableSet.of(READ), PUBLIC));

        FilteredResourceEnvelope result = service.filterResource(accessorId,
            ImmutableSet.of(idamRoleWithRoleBasedRestrictedAccess, idamRoleWithRoleBasedPrivateAccess),
            createResource(resourceId, resourceDefinition), attributePermissions);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .userSecurityClassification(RESTRICTED.toString())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(NAME_ATTRIBUTE), ImmutableSet.of(READ)))
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
                createResource(resourceId, resourceDefinition), attributePermissions);
        });
    }

    @Test
    void showOnlyVisibleAttributesForUseRoleBasedAccess() {
        Map<JsonPointer, SecurityClassification> securityClassificationMap = new ConcurrentHashMap<>();
        securityClassificationMap.put(JsonPointer.valueOf(""), PUBLIC);
        securityClassificationMap.put(JsonPointer.valueOf(NAME_ATTRIBUTE), PUBLIC);
        securityClassificationMap.put(JsonPointer.valueOf(CITY_ATTRIBUTE), PUBLIC);

        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, NAME_ATTRIBUTE, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(createDefaultPermissionGrant(idamRoleWithRoleBasedAccess,
            resourceDefinition, CITY_ATTRIBUTE, ImmutableSet.of(CREATE), PUBLIC));

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId,
                resourceDefinition), securityClassificationMap);

        JsonNode data = JsonNodeFactory.instance.objectNode()
            .put("name", "John")
            .set("address", JsonNodeFactory.instance.objectNode());

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PUBLIC.toString())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(NAME_ATTRIBUTE), ImmutableSet.of(READ),
                    // NO READ ACCESS FOR CITY ATTRIBUTE, SO SHOULD PERMISSIONS BE RETURNED?
                    JsonPointer.valueOf(CITY_ATTRIBUTE), ImmutableSet.of(CREATE)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    @Test
    void whenNoSecurityClassificationThenShouldInheritFromClosestParent() {
        Map<JsonPointer, SecurityClassification> securityClassifications = new ConcurrentHashMap<>();
        securityClassifications.put(JsonPointer.valueOf(""), PRIVATE);
        securityClassifications.put(JsonPointer.valueOf(NAME_ATTRIBUTE), PUBLIC);
        securityClassifications.put(JsonPointer.valueOf(ADDRESS_ATTRIBUTE), PUBLIC);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedAccess);

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedAccess), createResource(resourceId,
                resourceDefinition), securityClassifications);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .userSecurityClassification(PUBLIC.toString())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(NAME_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(ADDRESS_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(CITY_ATTRIBUTE), ImmutableSet.of(READ)))
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

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess), createResource(resourceId,
                resourceDefinition), securityClassifications);

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(DATA)
                .build())
            .userSecurityClassification(PRIVATE.toString())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(NAME_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(ADDRESS_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(CITY_ATTRIBUTE), ImmutableSet.of(READ)))
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
        securityClassifications.put(JsonPointer.valueOf(CITY_ATTRIBUTE), RESTRICTED);

        grantAllDefaultPermissionsForRole(idamRoleWithRoleBasedPrivateAccess);

        FilteredResourceEnvelope result = service.filterResource(
            accessorId, ImmutableSet.of(idamRoleWithRoleBasedPrivateAccess), createResource(resourceId,
                resourceDefinition), securityClassifications);

        JsonNode data = JsonNodeFactory.instance.objectNode()
            .put("name", "John")
            .set("address", JsonNodeFactory.instance.objectNode());

        assertThat(result).isEqualTo(FilteredResourceEnvelope.builder()
            .resource(Resource.builder()
                .id(resourceId)
                .definition(resourceDefinition)
                .data(data)
                .build())
            .userSecurityClassification(PRIVATE.toString())
            .access(AccessEnvelope.builder()
                .permissions(ImmutableMap.of(
                    JsonPointer.valueOf(""), ImmutableSet.of(READ),
                    JsonPointer.valueOf(NAME_ATTRIBUTE), ImmutableSet.of(READ),
                    JsonPointer.valueOf(ADDRESS_ATTRIBUTE), ImmutableSet.of(READ)))
                .accessType(ROLE_BASED)
                .build())
            .relationships(ImmutableSet.of())
            .build());
    }

    private void grantAllDefaultPermissionsForRole(String role) {
        importerService.grantDefaultPermission(
            createDefaultPermissionGrant(role, resourceDefinition, "", ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(
            createDefaultPermissionGrant(role, resourceDefinition, NAME_ATTRIBUTE, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(
            createDefaultPermissionGrant(role, resourceDefinition, ADDRESS_ATTRIBUTE, ImmutableSet.of(READ), PUBLIC));
        importerService.grantDefaultPermission(
            createDefaultPermissionGrant(role, resourceDefinition, CITY_ATTRIBUTE, ImmutableSet.of(READ), PUBLIC));
    }
}

