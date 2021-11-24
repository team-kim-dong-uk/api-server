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
import org.junit.jupiter.api.*;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    private static String userId = "6110066323a94f7c27f9cf4c";
    private static MockedStatic<SecurityUtils> mockedSecurityUtils;

    private UserDto userDto = UserDto.builder()
            .userId("6110066323a94f7c27f9cf4c").nickname("닉네임")
            .email("testuser@gmail.com")
            .numUploadedPhotos(100).numAlbumPhotos(4000)
            .build();
    private Photo photo = Photo.builder()
            .id(new ObjectId("6110066323a94f7c27f9cf4c"))
            .uploaderId(new ObjectId("6110066323a94f7c27f9cf4c"))
            .tags(List.of("오마이걸"))
            .build();
    private Feed feed = Feed.builder()
                            .id(new ObjectId("6110066323a94f7c27f9cf4c"))
                            .photo(photo)
                            .comments(new ArrayList<>())
                            .likes(new ArrayList<>())
                        .build();
    private Album album = Album.builder()
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
    @DisplayName("유저가 좋아요한 사진 조회")
    void getFeedLikedByUser() throws Exception {
        // given
        given(feedService.getLikedFeeds(userId, 20, 0))
                .willReturn(List.of(feed));
        given(albumService.findAllByUserIdAndFeedIdIn(userId, List.of()))
                .willReturn(List.of(album));
        // when
        String requestUri = "/api/v1/users/" + userId + "/like";
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
    @DisplayName("유저 상세정보 정상 호출")
    void detailUser_200() throws Exception {
        // given
        given(userService.getUserDetail(userId))
                .willReturn(userDto);

        // when
        String requestUri = "/api/v1/users/" + userId;
        ResultActions actions = mockMvc
                .perform(get(requestUri));
        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("userId", is(userId)))
                ;
    }

    @Test
    @WithMockUser
    @DisplayName("닉네임을 설정하는 테스트")
    void setNickname() throws Exception {
        String afterNickname = "afterSet";
        // given
        given(userService.setNickname(userId, afterNickname))
                .willReturn(UserDto.builder()
                        .nickname(afterNickname)
                        .build());

        // when
        String requestUri = "/api/v1/users/" + userId + "/nickname";
        ResultActions actions = mockMvc
                .perform(put(requestUri).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\": \""+afterNickname+"\"}")
                            .with(csrf().asHeader())
                );
        // then
        actions
                  .andExpect(status().isOk())
                /*.andExpect(status().isOk())
                .andExpect(jsonPath("userId", is(userId)))
                .andExpect(jsonPath("nickname", is(afterNickname)))*/
        ;
    }

    @Test
    @WithMockUser
    @DisplayName("유저 정보를 업데이트하는 테스트")
    void updateUser() throws Exception {
        String afterNickname = "afterSet";
        String group = "fromis";
        UpdateUserRequest request = UpdateUserRequest.builder()
                .nickname(afterNickname)
                .group(group)
                .build();
        // given
        given(userService.updateUser(matches(userId), any()))
                .willReturn(UserDto.builder()
                        .nickname(afterNickname)
                        .group(group)
                        .build());

        // when
        String requestUri = "/api/v1/users/" + userId;
        ResultActions actions = mockMvc
                .perform(patch(requestUri).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\": \""+afterNickname+"\"," +
                                "  \"group\": \""+group+"\"  }")
                        .with(csrf().asHeader())
                );
        // then
        actions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("nickname", is(afterNickname)))
                .andExpect(jsonPath("group", is(group)))
                ;
    }

    @Test
    @WithMockUser
    @DisplayName("업로드한 사진 목록 가져오기")
    void getUploadedByUser() throws Exception {
        // given
        given(photoService.findPhotosUploadedBy(userId, null, 21))
                .willReturn(List.of(PhotoOutlineDto.builder()
                        .build()));

        // when
        String requestUri = "/api/v1/users/" + userId + "/uploaded";
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
    @DisplayName("회원 탈퇴하기")
    void deleteUser() throws Exception {
        // when
        String requestUri = "/api/v1/users/" + userId;
        ResultActions actions = mockMvc
                .perform(delete(requestUri)
                        .with(csrf()));
        // then
        actions
                .andDo(print())
                .andExpect(status().isNoContent())
        ;
    }
    @Test
    @WithMockUser
    @DisplayName("존재하지 않는 회원 탈퇴하기")
    void deleteUser_404() throws Exception {
        doThrow(new UserNotFoundException(new ObjectId(userId)))
                .when(userService).deleteUser(userId);

        // when
        String requestUri = "/api/v1/users/" + userId;
        ResultActions actions = mockMvc
                .perform(delete(requestUri)
                        .with(csrf()));
        // then
        actions
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }
}
