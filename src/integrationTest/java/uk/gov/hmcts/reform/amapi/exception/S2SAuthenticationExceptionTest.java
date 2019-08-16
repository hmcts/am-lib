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
import uk.gov.hmcts.reform.amlib.AccessManagementService;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;

import java.nio.charset.StandardCharsets;

import static org.apache.http.entity.mime.MIME.CONTENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert","PMD.AvoidDuplicateLiterals","PMD.ExcessiveImports"})
public class S2SAuthenticationExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefaultRoleSetupImportService importerService;

    @MockBean
    private AccessManagementService accessManagementService;

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
    public void testHandleMissingBearerTokenException() throws Exception {

        String inputJson = Resources.toString(Resources
            .getResource("input-data/createResourceAccess.json"), StandardCharsets.UTF_8);

        doNothing().when(accessManagementService).grantExplicitResourceAccess(any());

        this.mockMvc.perform(post("/api/access-resource")
            .content(inputJson)
            .header(CONTENT_TYPE, APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isForbidden())
            /*.andExpect(jsonPath("$.errorMessage", is(ACCESS_DENIED)))
            .andExpect(jsonPath("$.status", is("FORBIDDEN")))
            .andExpect(jsonPath("$.errorCode", is(FORBIDDEN.value())))
            .andExpect(jsonPath("$.timeStamp", notNullValue()))
            .andExpect(jsonPath("$.errorDescription", notNullValue()))*/;

    }

}
