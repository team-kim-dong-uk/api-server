package com.udhd.apiserver.web;

import com.udhd.apiserver.config.auth.SecurityConfig;
import com.udhd.apiserver.config.auth.WebConfig;
import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.exception.user.UserNotFoundException;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.service.feed.FeedService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.feed.FeedDtoMapper;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.matches;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest( controllers = FeedController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {SecurityConfig.class, WebConfig.class})
        })
@ExtendWith(MockitoExtension.class)
public class FeedControllerSliceTest {

    @MockBean private FeedService feedService;
    @MockBean private AlbumService albumService;

    @Autowired
    protected MockMvc mockMvc;

    private static String userId = "6110066323a94f7c27f9cf4c";
    private static String feedId = "6110066323a94f7c27f9cf4d";
    private static String photoId = "6110066323a94f7c27f9cf4e";
    private static MockedStatic<SecurityUtils> mockedSecurityUtils;


    private Photo photo = Photo.builder()
            .id(new ObjectId(photoId))
            .uploaderId(new ObjectId(userId))
            .thumbnailLink("thumbnail.com")
            .scaledLink("scaled.com")
            .originalLink("origin.com")
            .checksum("123123123123")
            .tags(List.of("오마이걸"))
            .hash(Integer.toString(photoId.hashCode()))
            .build();
    private Feed feed = Feed.builder()
                            .id(new ObjectId(feedId))
                            .photo(photo)
                            .comments(new ArrayList<>())
                            .likes(new ArrayList<>())
                        .build();
    private Album album = Album.builder()
            .feedId(new ObjectId(feedId))
            .build();


    @BeforeAll
    static void beforeClass() {
        // static method mocking
        mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getLoginUserId).thenReturn(userId);
    }
    @AfterAll
    static void afterClass() { mockedSecurityUtils.close(); }


    @Test
    @WithMockUser
    @DisplayName("유저가 저장한 사진 조회")
    void getFeedSavedByUser() throws Exception {
        // given
        given(feedService.getSavedFeeds(userId, 20, 0))
                .willReturn(List.of(feed));
        // when
        String requestUri = "/api/v1/users/" + userId + "/save";
        ResultActions actions = mockMvc
                .perform(get(requestUri));
        // then
        actions
                .andDo(print())
                .andExpect(status().isOk())
        ;
    }
    @Test
    @WithMockUser
    @DisplayName("Feed List 가져오기")
    void getFeeds() throws Exception {
        // given
        given(feedService.getFeeds(userId)).willReturn(List.of(feed));
        given(albumService.findAllByUserIdAndFeedIdIn(any(), any()))
                .willReturn(List.of(album));
        // when
        String requestUri = "/api/v1/feeds/list";
        ResultActions actions = mockMvc
                .perform(get(requestUri));
        // then
        actions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("feeds").exists())
                .andExpect(jsonPath("feeds[0].saved").value(true))
        ;
    }
}
