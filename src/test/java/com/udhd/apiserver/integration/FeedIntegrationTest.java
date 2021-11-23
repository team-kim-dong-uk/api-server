package com.udhd.apiserver.integration;

import com.udhd.apiserver.integration.IntegrationTest;
import com.udhd.apiserver.web.UserController;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.user.UserDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class FeedIntegrationTest extends IntegrationTest {

    @Autowired
    protected UserController userController;


    private final PhotoOutlineDto mockPhotoOutlineDto = PhotoOutlineDto.builder()
            .photoId("456").thumbnailLink("http://link.com").build();

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
