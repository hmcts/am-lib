package integration.uk.gov.hmcts.reform.amlib;

import com.fasterxml.jackson.core.JsonPointer;
import com.google.common.collect.ImmutableMap;
import integration.uk.gov.hmcts.reform.amlib.base.PreconfiguredIntegrationBaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.FilterResourceResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.amlib.helpers.DefaultRoleSetupDataFactory.createDefaultPermissionGrant;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ACCESSOR_ID;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.CREATE_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.DATA;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.READ_PERMISSION;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAME;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROLE_NAMES;
import static uk.gov.hmcts.reform.amlib.helpers.TestConstants.ROOT_ATTRIBUTE;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createGrantForWholeDocument;
import static uk.gov.hmcts.reform.amlib.helpers.TestDataFactory.createResource;

class FilterResourceIntegrationTest extends PreconfiguredIntegrationBaseTest {
    private String resourceId;
    private static AccessManagementService ams;
    private static DefaultRoleSetupImportService rolesService;

    @BeforeAll
    static void setUp() {
        ams = new AccessManagementService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
        rolesService = new DefaultRoleSetupImportService(db.getJdbcUrl(), db.getUsername(), db.getPassword());
    }

    @BeforeEach
    void setupTest() {
        resourceId = UUID.randomUUID().toString();
    }

    @Test
    void whenRowExistsAndHasReadPermissionsShouldReturnEnvelopeWithData() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, READ_PERMISSION));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, null, createResource(resourceId));

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .data(DATA)
            .permissions(ImmutableMap.of(JsonPointer.valueOf(""), READ_PERMISSION))
            .build());
    }

    @Test
    void whenRowExistsAndDoesNotHaveReadPermissionsShouldReturnEnvelopeWithoutData() {
        ams.grantExplicitResourceAccess(createGrantForWholeDocument(resourceId, ACCESSOR_ID, CREATE_PERMISSION));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, null, createResource(resourceId));

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .data(null)
            .permissions(ImmutableMap.of(JsonPointer.valueOf(""), CREATE_PERMISSION))
            .build());
    }

    @Test
    void whenThereAreNoAccessRecordsShouldReturnNull() {
        String nonExistingUserId = "ijk";
        String nonExistingResourceId = "lmn";

        FilterResourceResponse result = ams.filterResource(
            nonExistingUserId, ROLE_NAMES, createResource(nonExistingResourceId));

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .data(null)
            .permissions(ImmutableMap.of(JsonPointer.valueOf(""), CREATE_PERMISSION))
            .build());
    }

    @Test
    void whenNoExplicitAccessShouldUseRoleBasedAccess() {
        rolesService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.ROLE_BASED);
        rolesService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, READ_PERMISSION));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isEqualTo(FilterResourceResponse.builder()
            .resourceId(resourceId)
            .data(DATA)
            .permissions(ImmutableMap.of(ROOT_ATTRIBUTE, READ_PERMISSION))
            .build());
    }

    @Test
    void whenNoExplicitAccessAndRoleHasExplicitAccessType() {
        rolesService.addRole(ROLE_NAME, RoleType.RESOURCE, SecurityClassification.PUBLIC, AccessType.EXPLICIT);
        rolesService.grantDefaultPermission(createDefaultPermissionGrant(ROOT_ATTRIBUTE, READ_PERMISSION));

        FilterResourceResponse result = ams.filterResource(ACCESSOR_ID, ROLE_NAMES, createResource(resourceId));

        assertThat(result).isNull();
    }
}
