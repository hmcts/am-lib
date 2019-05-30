package uk.gov.hmcts.reform.amapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateResourceAccess() throws Exception {

        String inputJson = "{\n"
            + "  \"resourceId\": \"1234\",\n"
            + "  \"resourceDefinition\": {\n"
            + "    \"serviceName\": \"cmc\",\n"
            + "    \"resourceType\": \"case\",\n"
            + "    \"resourceName\": \"claim\"},\n"
            + "  \"accessorIds\": [\"1234\"],\n"
            + "  \"accessorType\": \"USER\",\n"
            + "  \"attributePermissions\": {\n"
            + "    \"\": [\"CREATE\", \"READ\", \"UPDATE\"]\n"
            + "  },\n"
            + "  \"relationship\": \"caseworker\"\n"
            + "}";

        this.mockMvc.perform(post("/lib/create-resource-access")
            .content(inputJson)
            .header(CONTENT_TYPE, APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.resourceId", is("1234")))
            .andExpect(jsonPath("$.resourceDefinition.serviceName", is("cmc")))
            .andExpect(jsonPath("$.resourceDefinition.resourceType", is("case")))
            .andExpect(jsonPath("$.resourceDefinition.resourceName", is("claim")))
            .andExpect(jsonPath("$.accessorIds").value("1234"))
            .andExpect(jsonPath("$.accessorType", is("USER")))
            .andExpect(jsonPath("$.attributePermissions").exists())
            .andExpect(jsonPath("$.attributePermissions.*",hasItem(is(containsInAnyOrder("CREATE","READ","UPDATE")))))
            .andExpect(jsonPath("$.relationship", is("caseworker")));
    }

    @Test
    public void testRevokeResourceAccess() throws Exception {

        String inputJson = "{\n"
            + "  \"resourceId\": \"${resourceId}\",\n"
            + "  \"resourceDefinition\": {\n"
            + "    \"serviceName\": \"cmc\",\n"
            + "    \"resourceType\": \"case\",\n"
            + "    \"resourceName\": \"claim\"\n"
            + "  },\n"
            + "  \"accessorId\": \"${accessorId}\",\n"
            + "  \"accessorType\": \"USER\",\n"
            + "  \"attribute\": \"\",\n"
            + "  \"relationship\": \"caseworker\"\n"
            + "}";

        this.mockMvc.perform(delete("/lib/revoke-resource-access")
            .content(inputJson)
            .header(CONTENT_TYPE, APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    public void testFilterResource() throws Exception {

        String inputJson = "{\n"
            + "  \"userId\": \"1234\",\n"
            + "  \"userRoles\": [\n"
            + "    \"caseworker\"\n"
            + "  ],\n"
            + "  \"resource\": {\n"
            + "    \"id\": \"1234\",\n"
            + "    \"definition\": {\n"
            + "      \"serviceName\": \"cmc\",\n"
            + "      \"resourceType\": \"case\",\n"
            + "      \"resourceName\": \"claim\"\n"
            + "    },\n"
            + "    \"data\": {\n"
            + "      \"json\": \"resource\"\n"
            + "    }\n"
            + "  },\n"
            + "  \"attributeSecurityClassification\":{ \"\": \"PUBLIC\",\n"
            + "    \"/externalId\": \"PUBLIC\",\n"
            + "    \"/referenceNumber\": \"PUBLIC\",\n"
            + "    \"/referenceNumber/claimant\": \"PUBLIC\",\n"
            + "    \"/correspondenceAddress/line1\": \"PRIVATE\",\n"
            + "    \"/defendant/type\": \"RESTRICTED\"\n"
            + "  }\n"
            + "}\n"
            + "\n";

        this.testCreateResourceAccess();

        this.mockMvc.perform(post("/lib/filter-resource")
            .content(inputJson)
            .header(CONTENT_TYPE, APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resource.id", is("1234")))
            .andExpect(jsonPath("$.access.permissions").exists())
            .andExpect(jsonPath("$.access.permissions.*",hasItem(is(containsInAnyOrder("CREATE","READ","UPDATE")))))
            .andExpect(jsonPath("$.userSecurityClassification", is("PUBLIC")))
            .andExpect(jsonPath("$.resource.data.json", is("resource")));
    }
}
