package com.udhd.apiserver.web;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static capital.scalable.restdocs.misc.AuthorizationSnippet.documentAuthorization;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class UserControllerTest {
    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private UserService userService;
    @MockBean
    private PhotoService photoService;
    protected MockMvc mockMvc;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    private final PhotoOutlineDto mockPhotoOutlineDto = PhotoOutlineDto.builder()
            .photoId("456").thumbnailLink("http://link.com").build();

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
    void detailUser() throws Exception {
        // given
        String userId = "123";

        given(userService.getUserDetail(userId))
                .willReturn(UserDto.builder()
                        .userId("123").nickname("닉네임")
                        .email("testuser@gmail.com")
                        .numUploadedPhotos(100).numAlbumPhotos(4000)
                        .build());

        // when
        String requestUri = "/api/v1/users/" + userId;
        ResultActions actions = mockMvc
                .perform(get(requestUri).with(userToken()));

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    void setNickname() throws Exception {
        // given
        String userId = "123";
        String updateAlbumRequest = "{\"nickname\" : \"새 닉네임\"}";

        given(userService.setNickname(any(), any()))
                .willReturn(UserDto.builder()
                        .userId("123").nickname("새 닉네임")
                        .email("testuser@gmail.com")
                        .group("오마이걸")
                        .numUploadedPhotos(100).numAlbumPhotos(4000)
                        .build());

        // when
        String requestUri = "/api/v1/users/" + userId + "/nickname";
        ResultActions actions = mockMvc
                .perform(put(requestUri).with(userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAlbumRequest));

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    void setGroup() throws Exception {
        // given
        String userId = "123";
        String updateAlbumRequest = "{\"group\" : \"오마이걸\"}";

        given(userService.setGroup(any(), any()))
                .willReturn(UserDto.builder()
                        .userId("123").nickname("닉네임")
                        .email("testuser@gmail.com")
                        .group("오마이걸")
                        .numUploadedPhotos(100).numAlbumPhotos(4000)
                        .build());

        // when
        String requestUri = "/api/v1/users/" + userId + "/group";
        ResultActions actions = mockMvc
                .perform(put(requestUri).with(userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAlbumRequest));

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    void listUploaded() throws Exception {
        // given
        String userId = "123";

        given(photoService.findPhotosUploadedBy(userId, null, 21))
                .willReturn(Arrays.asList(mockPhotoOutlineDto, mockPhotoOutlineDto));

        // when
        String requestUri = "/api/v1/users/" + userId + "/uploaded?sortBy=id&fetchSize=21";
        ResultActions actions = mockMvc
                .perform(get(requestUri).with(userToken()));

        // then
        actions
                .andExpect(status().isOk());
    }

}
