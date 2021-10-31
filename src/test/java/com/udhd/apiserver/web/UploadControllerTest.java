package com.udhd.apiserver.web;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.config.auth.JwtAuthenticationFilter;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.domain.upload.Upload;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.UploadService;
import com.udhd.apiserver.util.JsonUtils;
import com.udhd.apiserver.util.JwtUtils;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.album.AlbumDetailDto;
import com.udhd.apiserver.web.dto.album.AlbumOutlineDto;
import com.udhd.apiserver.web.dto.upload.PresignedURLResponse;
import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
    @Mock
    private PhotoRepository photoRepository;

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
                        .withScheme("https")
                        .withHost("udhd.djbaek.com")
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
        String userId = "60e2fea74c17cf5152fb5b78";
        List<String> checksums = Arrays.asList(
            "912ec803b2ce49e4a541068d495ab570",
            "6a204bd89f3c8348afd5c77c717a097a");
        List<String> resultUrls = Arrays.asList("http://example-url", null);
        Map<String, Object> data = new HashMap<>();
        data.put("checksums", checksums);
        String presignedUrlRequest = JsonUtils.getInstance().stringify(data);

        String dummyPollingKey = uploadService.generatePollingKey(userId);

        PresignedURLResponse dummyRes = PresignedURLResponse.builder()
            .pollingKey(uploadService.generatePollingKey(userId))
            .checksums(checksums) /* Dummy md5 value */
            .urls(resultUrls)
            .build();

            List<Upload> dummyUploads = Arrays.asList(
                Upload.builder().pollingKey(dummyPollingKey)
                .checksum(checksums.get(0))
                .s3Url(resultUrls.get(0))
                .uploaderId(new ObjectId(userId))
                .build(),
                Upload.builder().pollingKey(dummyPollingKey)
                    .checksum(checksums.get(1))
                    .s3Url(resultUrls.get(1))
                    .uploaderId(new ObjectId(userId))
                    .build()
            );
        given(uploadService.createUpload(any(), any(), any()))
            .willReturn(dummyUploads);

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
