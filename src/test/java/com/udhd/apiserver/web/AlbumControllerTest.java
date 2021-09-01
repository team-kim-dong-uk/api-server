package com.udhd.apiserver.web;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.config.auth.JwtAuthenticationFilter;
import com.udhd.apiserver.service.AlbumService;
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
public class AlbumControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private AlbumService albumService;

    protected MockMvc mockMvc;

    private final AlbumDetailDto mockAlbumDetailDto
            = AlbumDetailDto.builder()
            .albumId("456")
            .uploaderId("123")
            .uploaderNickname("업로더")
            .originalLink("http://link.com/456")
            .favourite(true)
            .favouriteCount(532)
            .savedAt(new Date())
            .tags(Arrays.asList("오마이걸", "멤버1", "1집"))
            .build();

    private final AlbumOutlineDto mockAlbumOutlineDto
            = AlbumOutlineDto.builder()
            .albumId("456")
            .thumbnailLink("http://link.com/456")
            .build();


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
    void newAlbum() throws Exception {
        // given
        String userId = "123";
        String photoId = "456";
        String savePhotoRequest = "{\"photoId\" : \"" + photoId + "\"}";

        given(albumService.saveAlbum(userId, photoId)).willReturn(mockAlbumDetailDto);

        // when
        String requestUri = "/api/v1/users/" + userId + "/album";
        ResultActions actions = mockMvc
                .perform(post(requestUri).with(userToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(savePhotoRequest));

        // then
        actions
                .andExpect(status().isCreated());
    }

    @Test
    void detailAlbum() throws Exception {
        // given
        String userId = "123";
        String albumId = "456";

        given(albumService.getAlbumDetail(albumId)).willReturn(mockAlbumDetailDto);

        // when
        String requestUri = "/api/v1/users/" + userId + "/album/" + albumId;
        ResultActions actions = mockMvc
                .perform(get(requestUri).with(userToken()));

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    void listAlbum() throws Exception {
        // given
        String userId = "123";
        List<String> tags = Arrays.asList("오마이걸", "1집");

        given(albumService.findAlbums(userId, tags, null, 21)).willReturn(Arrays.asList(mockAlbumOutlineDto,
                mockAlbumOutlineDto));

        // when
        String requestUri = "/api/v1/users/" + userId + "/album?tags=오마이걸,1집&favouriteFirst" +
                "=true&sortBy=id&fetchSize=21";
        ResultActions actions = mockMvc
                .perform(get(requestUri).with(userToken()));

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    void updateAlbumFavourite() throws Exception {
        // given
        String userId = "123";
        String albumId = "456";
        String updateAlbumFavouriteRequest = "{\"favourite\" : true}";

        given(albumService.updateAlbumFavourite(userId, albumId, true)).willReturn(mockAlbumDetailDto);

        // when
        String requestUri = "/api/v1/users/" + userId + "/album/" + albumId + "/favourite";
        ResultActions actions = mockMvc
                .perform(patch(requestUri).with(userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAlbumFavouriteRequest));

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    void updateAlbumTags() throws Exception {
        // given
        String userId = "123";
        String albumId = "456";
        String updateAlbumTagsRequest = "{\"tags\" : [\"오마이걸\", \"멤버1\", \"1집\"]}";

        given(albumService.updateAlbumTags(any(), any(), any()))
                .willReturn(mockAlbumDetailDto);

        // when
        String requestUri = "/api/v1/users/" + userId + "/album/" + albumId + "/tags";
        ResultActions actions = mockMvc
                .perform(patch(requestUri).with(userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAlbumTagsRequest));

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    void deleteAlbum() throws Exception {
        // given
        String userId = "123";
        String albumId = "456";

        doNothing().when(albumService).deleteAlbum(userId, albumId);

        // when
        String requestUri = "/api/v1/users/" + userId + "/album/" + albumId;
        ResultActions actions = mockMvc
                .perform(delete(requestUri).with(userToken()));

        // then
        actions
                .andExpect(status().isNoContent());
    }
}
