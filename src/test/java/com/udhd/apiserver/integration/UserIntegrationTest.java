package com.udhd.apiserver.integration;

import com.udhd.apiserver.integration.IntegrationTest;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.user.UserDto;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserIntegrationTest extends IntegrationTest {

    @MockBean
    private UserService userService;
    @MockBean
    private PhotoService photoService;

    private final PhotoOutlineDto mockPhotoOutlineDto = PhotoOutlineDto.builder()
            .photoId("456").thumbnailLink("http://link.com").build();


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

    @Test
    void deleteUser() throws Exception {
        String userId = "123";

        var requestUri =  "/api/v1/users/" + userId;
        ResultActions actions = mockMvc
                .perform(delete(requestUri).with(userToken()));

        actions
                .andExpect(status().isNoContent());

    }

    @Test
    void deleteUser_404() throws Exception {
        String userId = "123444";
        String errorMessage = "error in findById";

        doThrow(new IllegalArgumentException(errorMessage)).when(userService).deleteUser(userId);

        var requestUri =  "/api/v1/users/" + userId;
        ResultActions actions = mockMvc
                .perform(delete(requestUri).with(userToken()));

        actions
                .andExpect(status().isNotFound())
        ;

    }

}
