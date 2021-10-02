package com.udhd.apiserver.web;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.domain.tag.Tag;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.search.SearchService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.search.SearchCandidateDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class SearchControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private PhotoService photoService;
    @MockBean
    private SearchService searchService;
    @MockBean
    private AlbumService albumService;

    protected MockMvc mockMvc;

    private final PhotoOutlineDto mockPhotoOutlineDto = PhotoOutlineDto.builder()
            .photoId("6110066323a94f7c27f9cf4c").thumbnailLink("http://link.com").build();

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeAll
    public void mockStaticSetup() {
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        given(SecurityUtils.getLoginUserId()).willReturn("60e2fea74c17cf5152fb5b78");
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
    void tagsRecommended() throws Exception {
        // given
        String userId = "60e2fea74c17cf5152fb5b78";
        String keyword = "오마이걸";

        given(searchService.getRecommendedKeywords("오마이걸")).willReturn(Arrays.asList(
                SearchCandidateDto.fromTag(new Tag("오마이걸", 400)),
                SearchCandidateDto.fromTag(new Tag("오마이걸시작태그", 100)),
                SearchCandidateDto.fromTag(new Tag("포함오마이걸", 230)),
                SearchCandidateDto.fromUser(User.builder().nickname("오마이걸시작닉네임").id(new ObjectId(
                        "60e2fea74c17cf5152fb5b78")).uploadCount(3).build())
        ));

        System.out.println(searchService.getRecommendedKeywords("오마이걸"));
        // when
        String requestUri = "/api/v1/users/" + userId + "/search/tags/recommended?keyword=" + keyword;
        ResultActions actions = mockMvc
                .perform(get(requestUri).with(userToken()));

        // then
        actions
                .andExpect(status().isOk());
    }


    @Test
    void searchTags() throws Exception {
        // given
        String userId = "60e2fea74c17cf5152fb5b78";
        List<String> tags = Arrays.asList("오마이걸", "1집");
        List<String> photoIds = Arrays.asList("6110066323a94f7c27f9cf4c");

        given(photoService.findPhotos(tags, null, userId, null, 21))
            .willReturn(Arrays.asList(mockPhotoOutlineDto));
        given(searchService.remainNotOwned(userId, photoIds))
            .willReturn(photoIds);

        // when
        String requestUri = "/api/v1/users/" + userId + "/search?tags=오마이걸,1집&sortBy=random&fetchSize=21";
        ResultActions actions = mockMvc
                .perform(get(requestUri).with(userToken()));

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    void searchSimilar() throws Exception {
        // given
        String userId = "60e2fea74c17cf5152fb5b78";
        String photoId = "6110066323a94f7c27f9cf4c";

        // when
        String requestUri = "/api/v1/users/" + userId + "/search/similar/" + photoId;
        ResultActions actions = mockMvc
                .perform(get(requestUri).with(userToken()));

        // then
        actions
                .andExpect(status().isOk());
    }
}
