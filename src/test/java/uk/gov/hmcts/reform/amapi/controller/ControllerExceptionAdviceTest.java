package uk.gov.hmcts.reform.amapi.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.apache.http.entity.mime.MIME.CONTENT_TYPE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;
import static uk.gov.hmcts.reform.amapi.util.ErrorConstants.MALFORMED_JSON;
import static uk.gov.hmcts.reform.amapi.util.ErrorConstants.RESOURCE_NOT_FOUND;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class ControllerExceptionAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test Controller Exception Handler Message Not readable.
     *
     * @throws Exception when exceptional condition happens
     */
    @Test
    public void testHandleHttpMessageNotReadable() throws Exception {

        String invalidJson = "{\n"
            + "  \"userId\": \"${accessorId}\",\n"
            + "  \"userRoles\": [\n"
            + "    \"caseworker\"\n"
            + "  ],\n"
            + "  \"resource\": {\n"
            + "    \"id\": \"${resourceId}\",\n"
            + "    \"definition\": {\n"
            + "      \"serviceName\": \"cmc\",\n"
            + "      \"resourceType\": \"case\",\n"
            + "      \"resourceName\": \"claim\"\n"
            + "    },\n"
            + "    \"data\": {\n"
            + "      \"json\": \"resource\"\n"
            + "    }\n"
            + "  },,,";

        this.mockMvc.perform(post("/lib/filter-resource")
            .content(invalidJson)
            .header(CONTENT_TYPE, APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorMessage", is(MALFORMED_JSON)))
            .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
            .andExpect(jsonPath("$.errorCode", is(BAD_REQUEST.value())))
            .andExpect(jsonPath("$.timeStamp", notNullValue()))
            .andExpect(jsonPath("$.errorDescription", notNullValue()));
    }

    /**
     * Test Media type not supported.
     *
     * @throws Exception when exceptional condition happens
     */
    @Test
    public void testHandleHttpMediaTypeNotSupported() throws Exception {

        String invalidJson = "{\n"
            + "  \"userId\": \"${accessorId}\",\n"
            + "  \"userRoles\": [\n"
            + "    \"caseworker\"\n"
            + "  ],\n"
            + "  \"resource\": {\n"
            + "    \"id\": \"${resourceId}\",\n"
            + "    \"definition\": {\n"
            + "      \"serviceName\": \"cmc\",\n"
            + "      \"resourceType\": \"case\",\n"
            + "      \"resourceName\": \"claim\"\n"
            + "    },\n"
            + "    \"data\": {\n"
            + "      \"json\": \"resource\"\n"
            + "    }\n"
            + "  }";

        this.mockMvc.perform(post("/lib/filter-resource")
            .content(invalidJson)
            .header(CONTENT_TYPE, TEXT_HTML))
            .andDo(print())
            .andExpect(status().isUnsupportedMediaType());
    }

    /**
     * Resource Not found test.
     *
     * @throws Exception when exceptional condition happens
     */
    @Test
    public void testHandleNoHandlerFoundException() throws Exception {

        String invalidJson = "";

        this.mockMvc.perform(post("/invalidUrl")
            .content(invalidJson)
            .header(CONTENT_TYPE, APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorMessage", is(RESOURCE_NOT_FOUND)))
            .andExpect(jsonPath("$.status", is("NOT_FOUND")))
            .andExpect(jsonPath("$.errorCode", is(NOT_FOUND.value())))
            .andExpect(jsonPath("$.timeStamp", notNullValue()))
            .andExpect(jsonPath("$.errorDescription", is(RESOURCE_NOT_FOUND)));

    }


    /**
     * Internal server error check.
     *
     * @throws Exception when exceptional condition happens
     */
    @Test
    public void testHandleInternalServerErrors() throws Exception {

        String invalidJson = "{\n"
            + "  \"userId\": \"${accessorId}\",\n"
            + "  \"userRoles\": [\n"
            + "    \"caseworker\"\n"
            + "  ],\n"
            + "  \"resource\": {\n"
            + "    \"id\": \"${resourceId}\",\n"
            + "    \"definition\": {\n"
            + "      \"serviceName\": \"cmc\",\n"
            + "      \"resourceType\": \"case\",\n"
            + "      \"resourceName\": \"claim\"\n"
            + "    },\n"
            + "    \"data\": {\n"
            + "      \"json\": \"resource\"\n"
            + "    }\n"
            + "  },\n"
            + "  \"attributeSecurityClassification\":{ \n"
            + "    \"/externalId\": \"PUBLIC\"\n"
            + "  \n"
            + "  }\n"
            + "}\n";

        this.mockMvc.perform(post("/lib/filter-resource")
            .content(invalidJson)
            .header(CONTENT_TYPE, APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorMessage", is("Root element not found in input Security Classification")))
            .andExpect(jsonPath("$.status", is("INTERNAL_SERVER_ERROR")))
            .andExpect(jsonPath("$.errorCode", is(INTERNAL_SERVER_ERROR.value())))
            .andExpect(jsonPath("$.timeStamp", notNullValue()))
            .andExpect(jsonPath("$.errorDescription", notNullValue()));
    }

}
