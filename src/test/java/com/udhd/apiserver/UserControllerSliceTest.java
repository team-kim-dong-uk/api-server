package com.udhd.apiserver;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.config.auth.SecurityConfig;
import com.udhd.apiserver.config.auth.WebConfig;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.AuthService;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.service.feed.FeedService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.UserController;
import com.udhd.apiserver.web.dto.feed.FeedDtoMapper;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.user.UserDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static capital.scalable.restdocs.misc.AuthorizationSnippet.documentAuthorization;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest( controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, WebConfig.class})
        })
@ExtendWith(MockitoExtension.class)
public class UserControllerSliceTest {

    @MockBean private UserService userService;
    @MockBean private PhotoService photoService;
    @MockBean private FeedService feedService;
    @MockBean private AlbumService albumService;
    @MockBean private FeedDtoMapper feedDtoMapper;

    @Autowired
    protected MockMvc mockMvc;

    @Test
    @WithMockUser
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
                .perform(get(requestUri)
                        //.with(userToken())
                )
                ;

        // then
        actions
                .andExpect(status().isOk());
    }


}
