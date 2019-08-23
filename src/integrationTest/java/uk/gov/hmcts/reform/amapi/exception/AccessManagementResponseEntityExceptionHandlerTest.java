package uk.gov.hmcts.reform.amapi.exception;

import com.google.common.io.Resources;
import org.junit.jupiter.api.AfterEach;
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
import uk.gov.hmcts.reform.amapi.controllers.SecurityAuthorizationTest;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import java.nio.charset.StandardCharsets;

import static org.apache.http.entity.mime.MIME.CONTENT_TYPE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;
import static uk.gov.hmcts.reform.amapi.util.ErrorConstants.MALFORMED_JSON;

//import static org.springframework.http.HttpStatus.FORBIDDEN;
//import static uk.gov.hmcts.reform.amapi.util.ErrorConstants.ACCESS_DENIED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert","PMD.AvoidDuplicateLiterals","PMD.ExcessiveImports"})
public class AccessManagementResponseEntityExceptionHandlerTest extends SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefaultRoleSetupImportService importerService;

    private String s2sToken;

    @BeforeEach
    void init() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        doNothing().when(importerService).addService(anyString());
        doNothing().when(importerService).addResourceDefinition(any());
        doNothing().when(importerService).addRole(anyString(), any(), any(), any());

        s2sToken = getS2sToken();
        //s2sToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbV9hY2Nlc3NtZ210X2FwaSIsImV4cCI6MTU2NDc0MjcxN30.rIpRPLo3r"
        //    + "XGex6iZcq1kG1732h53P744Fq5NTTKqw33jlMotC7jDuOffyCnerXyQxjTuN93F2Iuu7gY3NJ99Pw";
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

        this.mockMvc.perform(post("/api/v1/filter-resource")
            .content(invalidJson)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header("ServiceAuthorization", s2sToken))
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

        this.mockMvc.perform(post("/api/v1/filter-resource")
            .content(invalidJson)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header("ServiceAuthorization", s2sToken))
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
            .getResource("input-data/filterResourceWithSecurityClassification.json"), StandardCharsets.UTF_8);

        this.mockMvc.perform(post("/api/v1/filter-resource")
            .content(inputJson)
            .header(CONTENT_TYPE, TEXT_HTML)
            .header("ServiceAuthorization", s2sToken))
            .andDo(print())
            .andExpect(status().isUnsupportedMediaType());
    }

    /**
     * Resource Not found test.
     *
     * @throws Exception when exceptional condition happens
     */
    /*@Test
    public void testHandleNoHandlerFoundException() throws Exception {

        String invalidJson = "";

        this.mockMvc.perform(post("/invalidUrl")
            .content(invalidJson)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header("ServiceAuthorization", s2sToken))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorMessage", is(RESOURCE_NOT_FOUND)))
            .andExpect(jsonPath("$.status", is("NOT_FOUND")))
            .andExpect(jsonPath("$.errorCode", is(NOT_FOUND.value())))
            .andExpect(jsonPath("$.timeStamp", notNullValue()))
            .andExpect(jsonPath("$.errorDescription", is(RESOURCE_NOT_FOUND)));

    }*/


    /**
     * Internal server error check.
     *
     * @throws Exception when exceptional condition happens
     */
    @Test
    public void testMissingSecurityClassificationRootError() throws Exception {

        String invalidJson = Resources.toString(Resources
            .getResource("exception-mapper-data/filterResourceWithMissingRoot.json"), StandardCharsets.UTF_8);

        this.mockMvc.perform(post("/api/v1/filter-resource")
            .content(invalidJson)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .header("ServiceAuthorization", s2sToken))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorMessage", is(
                "filterResource.attributeSecurityClassifications - must contain root attribute")))
            .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
            .andExpect(jsonPath("$.errorCode", is(BAD_REQUEST.value())))
            .andExpect(jsonPath("$.timeStamp", notNullValue()))
            .andExpect(jsonPath("$.errorDescription", notNullValue()));
    }


    @AfterEach
    @Override
    public void tearDown() {
        //doNothing
    }
}
