package uk.gov.hmcts.reform.amapi.exception;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import java.nio.charset.StandardCharsets;

import static org.apache.http.entity.mime.MIME.CONTENT_TYPE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
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
@ActiveProfiles("test")
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert","PMD.AvoidDuplicateLiterals","PMD.ExcessiveImports"})
public class AccessManagementResponseEntityExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefaultRoleSetupImportService importerService;

    @BeforeEach
    void init() {
        doNothing().when(importerService).addService(anyString());
        doNothing().when(importerService).addResourceDefinition(any());
        doNothing().when(importerService).addRole(anyString(), any(), any(), any());
    }

    /**
     * Test Controller Exception Handler Message Not readable.
     *
     * @throws Exception when exceptional condition happens
     */
    @Test
    public void testHandleHttpMessageNotReadable() throws Exception {

        String invalidJson = Resources.toString(Resources
            .getResource("exception-mapper-data/malformedInput.json"), StandardCharsets.UTF_8);

        this.mockMvc.perform(post("/api/filter-resource")
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
     * Test Controller Exception Handler Message Not readable.
     *
     * @throws Exception when exceptional condition happens
     */
    @Test
    public void testHandleMissingInputParameterException() throws Exception {

        String invalidJson = Resources.toString(Resources
            .getResource("exception-mapper-data/missingValidInputParameter.json"), StandardCharsets.UTF_8);

        this.mockMvc.perform(post("/api/filter-resource")
            .content(invalidJson)
            .header(CONTENT_TYPE, APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorMessage", is("filterResource.resource.id - must not be blank")))
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

        String inputJson = Resources.toString(Resources
            .getResource("input-data/filterResource.json"), StandardCharsets.UTF_8);

        this.mockMvc.perform(post("/api/filter-resource")
            .content(inputJson)
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
}
