package com.udhd.apiserver.web;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.service.feed.FeedException;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.user.UserDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static capital.scalable.restdocs.misc.AuthorizationSnippet.documentAuthorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class FeedControllerTest {
    @Mock
    private PhotoRepository photoRepository;

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserController userController;
    @Value("${userId}")
    private String userId;
    @Value("${feedId}")
    private String feedId;

    protected MockMvc mockMvc;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    private final PhotoOutlineDto mockPhotoOutlineDto = PhotoOutlineDto.builder()
            .photoId("456").thumbnailLink("http://link.com").build();

    @BeforeAll
    public void mockStaticSetup() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        given(SecurityUtils.getLoginUserId()).willReturn("612e4785662b04006f78157d");
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
    @DisplayName("Feed Like")
    void likeFeed() throws Exception {
        String requestUri = "/api/v1/feeds/" + feedId + "/like";
        // clear feed
        mockMvc.perform(delete(requestUri)
                .with(userToken())
        );

        UserDto beforeUser = userController.detailUser(userId);
        mockMvc.perform(put(requestUri)
                .with(userToken())
        )
                .andExpect(status().isOk())
        ;
        UserDto afterAddUser = userController.detailUser(userId);
        assertThat(beforeUser.getNumLikePhotos()+1).isEqualTo(afterAddUser.getNumLikePhotos());

        mockMvc.perform(delete(requestUri)
                        .with(userToken())
                )
                .andExpect(status().isOk())
        ;
        UserDto afterDeleteUser = userController.detailUser(userId);
        assertThat(afterDeleteUser.getNumLikePhotos()).isEqualTo(beforeUser.getNumLikePhotos());
    }
    @Test
    @DisplayName("좋아요한 사진 중복 추가")
    void duplicatedLike() throws Exception {
        String requestUri = "/api/v1/feeds/" + feedId + "/like";
        // 일단 삭제
        mockMvc.perform(delete(requestUri)
                .with(userToken())
        );
        // 1차 추가
        mockMvc.perform(put(requestUri)
                        .with(userToken())
                )
                .andExpect(status().isOk())
        ;
        // 2차 추가
        MvcResult actions = mockMvc.perform(put(requestUri)
                        .with(userToken())
                )
                .andExpect(status().isBadRequest())
                .andReturn()
        ;
    }

    @Test
    @DisplayName("Feed Save")
    void saveFeed() throws Exception {
        String requestUri = "/api/v1/feeds/" + feedId + "/save";
        // clear feed
        mockMvc.perform(delete(requestUri)
                .with(userToken())
        );

        UserDto beforeUser = userController.detailUser(userId);
        mockMvc.perform(put(requestUri)
                        .with(userToken())
                )
                .andExpect(status().isOk())
        ;
        UserDto afterAddUser = userController.detailUser(userId);
        assertThat(beforeUser.getNumSavePhotos()+1).isEqualTo(afterAddUser.getNumSavePhotos());

        mockMvc.perform(delete(requestUri)
                        .with(userToken())
                )
                .andExpect(status().isOk())
        ;
        UserDto afterDeleteUser = userController.detailUser(userId);
        assertThat(afterDeleteUser.getNumSavePhotos()).isEqualTo(beforeUser.getNumSavePhotos());
    }
}
