package com.udhd.apiserver.web;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.config.auth.JwtAuthenticationFilter;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.UploadService;
import com.udhd.apiserver.util.JwtUtils;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.album.AlbumDetailDto;
import com.udhd.apiserver.web.dto.album.AlbumOutlineDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static capital.scalable.restdocs.misc.AuthorizationSnippet.documentAuthorization;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class UploadControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private UploadService uploadService;

    protected MockMvc mockMvc;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeAll
    public void mockStaticSetup() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        given(SecurityUtils.getLoginUserId()).willReturn("123");
    }

    @AfterAll
    public void demockStaticSetup() {
        mockedSecurityUtils.close();
    }

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(JacksonResultHandlers.prepareJackson(objectMapper))
                .alwaysDo(MockMvcRestDocumentation.document("{class-name}/{method-name}",
                        Preprocessors.preprocessRequest(),
                        Preprocessors.preprocessResponse(
                                ResponseModifyingPreprocessors.replaceBinaryContent(),
                                ResponseModifyingPreprocessors.limitJsonArrayLength(objectMapper),
                                Preprocessors.prettyPrint())))
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                        .uris()
                        .withScheme("http")
                        .withHost("udhd.djbaek.com")
                        .withPort(8080)
                        .and().snippets()
                        .withDefaults(CliDocumentation.curlRequest(),
                                HttpDocumentation.httpRequest(),
                                HttpDocumentation.httpResponse(),
                                AutoDocumentation.requestFields(),
                                AutoDocumentation.responseFields(),
                                AutoDocumentation.pathParameters(),
                                AutoDocumentation.requestParameters(),
                                AutoDocumentation.description(),
                                AutoDocumentation.methodAndPath(),
                                AutoDocumentation.authorization("User access token required."),
                                AutoDocumentation.sectionBuilder()
                                        .snippetNames(
                                                SnippetRegistry.AUTO_AUTHORIZATION,
                                                SnippetRegistry.AUTO_PATH_PARAMETERS,
                                                SnippetRegistry.AUTO_REQUEST_PARAMETERS,
                                                SnippetRegistry.AUTO_REQUEST_FIELDS,
                                                SnippetRegistry.HTTP_REQUEST,
                                                SnippetRegistry.AUTO_RESPONSE_FIELDS,
                                                SnippetRegistry.HTTP_RESPONSE)
                                        .skipEmpty(true)
                                        .build()))
                .build();
    }

    protected RequestPostProcessor userToken() {
        return (request) -> {
            request.addHeader("Authorization", "Bearer <access-token>");
            return documentAuthorization(request, "User access token required.");
        };
    }

    @Test
    void presignedUrls() throws Exception {
        // given
        String userId = "123";
        String presignedUrlRequest = "{\"checksums\" : [\"absbsb\", \"absbsb\"]}";

        given(uploadService.getPreSignedURLs(any()))
                .willReturn(Arrays.asList("http://example-url", null));

        // when
        String requestUri = "/api/v1/upload/presigned-url";
        ResultActions actions = mockMvc
                .perform(post(requestUri).with(userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(presignedUrlRequest));

        // then
        actions
                .andExpect(status().isOk());
    }


}
