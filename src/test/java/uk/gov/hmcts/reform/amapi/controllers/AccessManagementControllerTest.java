package uk.gov.hmcts.reform.amapi.controllers;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.amapi.models.FilterResource;
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.FilterResourceService;
import uk.gov.hmcts.reform.amlib.enums.Permission;
import uk.gov.hmcts.reform.amlib.enums.SecurityClassification;
import uk.gov.hmcts.reform.amlib.models.Resource;
import uk.gov.hmcts.reform.amlib.models.FilteredResourceEnvelope;
import uk.gov.hmcts.reform.amlib.models.ResourceAccessorsEnvelope;
import uk.gov.hmcts.reform.amlib.models.ResourceAccessor;
import uk.gov.hmcts.reform.amlib.models.AccessEnvelope;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;
import static uk.gov.hmcts.reform.amlib.enums.AccessorType.USER;
import static uk.gov.hmcts.reform.amlib.enums.Permission.CREATE;
import static uk.gov.hmcts.reform.amlib.enums.Permission.READ;
import static uk.gov.hmcts.reform.amlib.enums.Permission.UPDATE;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.ExcessiveImports", "PMD.LawOfDemeter",
    "PMD.AvoidDuplicateLiterals"})
public class AccessManagementControllerTest extends SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefaultRoleSetupImportService importerService;

    @MockBean
    private AccessManagementService accessManagementService;

    @MockBean
    private FilterResourceService filterResourceService;

    private String s2sToken;

    @BeforeEach
    void init() {
        doNothing().when(importerService).addService(anyString());
        doNothing().when(importerService).addResourceDefinition(any());
        doNothing().when(importerService).addRole(anyString(), any(), any(), any());

        s2sToken = getS2sToken();
    }

    @Test
    public void testCreateResourceAccess() throws Exception {

        String inputJson = Resources.toString(Resources
            .getResource("input-data/createResourceAccess.json"), StandardCharsets.UTF_8);

        doNothing().when(accessManagementService).grantExplicitResourceAccess(any());

        this.mockMvc.perform(post("/api/access-resource")
            .content(inputJson)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header("ServiceAuthorization", s2sToken))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resourceId", is("1234")))
            .andExpect(jsonPath("$.resourceDefinition.serviceName", is("cmc")))
            .andExpect(jsonPath("$.resourceDefinition.resourceType", is("case")))
            .andExpect(jsonPath("$.resourceDefinition.resourceName", is("claim")))
            .andExpect(jsonPath("$.accessorIds").value("12345"))
            .andExpect(jsonPath("$.accessorType", is("USER")))
            .andExpect(jsonPath("$.attributePermissions").exists())
            .andExpect(jsonPath("$.attributePermissions.*",
                hasItem(is(containsInAnyOrder("CREATE", "READ", "UPDATE")))))
            .andExpect(jsonPath("$.relationship", is("caseworker")));
    }

    @Test
    public void testRevokeResourceAccess() throws Exception {

        String inputJson = Resources.toString(Resources
            .getResource("input-data/revokeResourceAccess.json"), StandardCharsets.UTF_8);

        doNothing().when(accessManagementService).revokeResourceAccess(any());

        this.mockMvc.perform(delete("/api/access-resource")
            .content(inputJson)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header("ServiceAuthorization", s2sToken))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    public void testFilterResource() throws Exception {

        String inputJson = Resources.toString(Resources
            .getResource("input-data/filterResource.json"), StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        final FilterResource filterResource = mapper.readValue(inputJson, FilterResource.class);

        Set<Permission> permissions = new HashSet<>();
        permissions.add(CREATE);
        permissions.add(READ);
        permissions.add(UPDATE);

        Map<JsonPointer, Set<Permission>> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), permissions);
        AccessEnvelope envelope = AccessEnvelope.builder().permissions(attributePermissions).build();

        Resource resource = Resource.builder().id("1234").data(JsonNodeFactory.instance.objectNode()
            .put("json", "resource")).build();

        FilteredResourceEnvelope filteredResourceEnvelope = FilteredResourceEnvelope.builder()
            .resource(resource).access(envelope).build();

        Mockito.when(filterResourceService.filterResource(filterResource.getUserId(),
            filterResource.getUserRoles(),
            filterResource.getResource(),
            filterResource.getAttributeSecurityClassification()))
            .thenReturn(filteredResourceEnvelope);

        this.mockMvc.perform(post("/api/filter-resource")
            .content(inputJson)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header("ServiceAuthorization", s2sToken))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resource.id", is("1234")))
            .andExpect(jsonPath("$.access.permissions").exists())
            .andExpect(jsonPath("$.access.permissions.*", hasItem(is(containsInAnyOrder("CREATE", "READ", "UPDATE")))))
            .andExpect(jsonPath("$.resource.data.json", is("resource")));
    }

    @Test
    public void testFilterResourceWithSecurityClassification() throws Exception {

        String inputJson = Resources.toString(Resources
            .getResource("input-data/filterResourceWithSecurityClassification.json"), StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        final FilterResource filterResource = mapper.readValue(inputJson,
            FilterResource.class);

        Set<Permission> permissions = new HashSet<>();
        permissions.add(CREATE);
        permissions.add(READ);
        permissions.add(UPDATE);

        Map<JsonPointer, Set<Permission>> attributePermissions = new ConcurrentHashMap<>();
        attributePermissions.put(JsonPointer.valueOf(""), permissions);
        AccessEnvelope envelope = AccessEnvelope.builder().permissions(attributePermissions).build();

        Resource resource = Resource.builder().id("1234").data(JsonNodeFactory.instance.objectNode()
            .put("json", "resource")).build();

        FilteredResourceEnvelope filteredResourceEnvelope = FilteredResourceEnvelope.builder()
            .resource(resource).access(envelope).userSecurityClassification(SecurityClassification.PUBLIC).build();

        Mockito.when(filterResourceService.filterResource(filterResource.getUserId(),
            filterResource.getUserRoles(),
            filterResource.getResource(), filterResource.getAttributeSecurityClassification()))
            .thenReturn(filteredResourceEnvelope);

        this.mockMvc.perform(post("/api/filter-resource")
            .content(inputJson)
            .header("ServiceAuthorization", s2sToken)
            .header(CONTENT_TYPE, APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resource.id", is("1234")))
            .andExpect(jsonPath("$.access.permissions").exists())
            .andExpect(jsonPath("$.access.permissions.*", hasItem(is(containsInAnyOrder("CREATE", "READ", "UPDATE")))))
            .andExpect(jsonPath("$.userSecurityClassification", is("PUBLIC")))
            .andExpect(jsonPath("$.resource.data.json", is("resource")));
    }

    @Test
    public void testReturnResourceAccessors() throws Exception {

        String resourceType = "case";
        String resourceName = "claim";
        String resourceId = "0011";

        ResourceAccessorsEnvelope resourceAccessorsEnvelope = ResourceAccessorsEnvelope.builder()
            .resourceId(resourceId)
            .explicitAccessors(Collections.singletonList(ResourceAccessor.builder()
                .accessorId("5511")
                .accessorType(USER)
                .relationships(ImmutableSet.of("caseworker"))
                .permissions(Collections.singletonMap(JsonPointer.valueOf(""), ImmutableSet.of(READ, UPDATE, CREATE)))
                .build()))
            .build();

        Mockito.when(filterResourceService.returnResourceAccessors(resourceId, resourceName, resourceType))
            .thenReturn(resourceAccessorsEnvelope);

        this.mockMvc.perform(get("/api/resource/resourceType/" + resourceType + "/resourceName/"
            + resourceName + "/resourceId/" + resourceId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resourceId", is("0011")))
            .andExpect(jsonPath("$.explicitAccess").exists())
            .andExpect(jsonPath("$.explicitAccess[0].accessorId", is("5511")))
            .andExpect(jsonPath("$.explicitAccess[0].accessorType", is("USER")))
            .andExpect(jsonPath("$.explicitAccess[0].relationships.*", containsInAnyOrder("caseworker")))
            .andExpect(jsonPath("$.explicitAccess[0].permissions.*",
                hasItem(is(containsInAnyOrder("CREATE", "READ", "UPDATE")))));
    }

    @AfterEach
    @Override
    public void tearDown() {
        //doNothing
    }
}
