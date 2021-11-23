package com.udhd.apiserver.integration;

import com.udhd.apiserver.integration.IntegrationTest;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.web.dto.photo.PhotoDetailDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Date;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PhotoIntegrationTest extends IntegrationTest {

    @MockBean
    private PhotoService photoService;

    private final PhotoDetailDto mockPhotoDetailDto
            = PhotoDetailDto.builder()
            .photoId("456")
            .uploaderId("123")
            .uploaderNickname("업로더")
            .originalLink("http://link.com/456")
            .uploadedAt(new Date())
            .tags(Arrays.asList("오마이걸", "멤버1", "1집", "210701"))
            .build();

    @Test
    void uploadPhotos() throws Exception {
        // given

        // when
        String requestUri = "/api/v1/photos";
        ResultActions actions = mockMvc
                .perform(post(requestUri));

        // then
        actions
                .andExpect(status().isCreated());
    }

    @Test
    void recommendTags() throws Exception {
        // given

        // when
        String requestUri = "/api/v1/photos/recommend/tags";
        ResultActions actions = mockMvc
                .perform(post(requestUri));

        // then
        actions
                .andExpect(status().isOk());
    }

    @Test
    void detailPhoto() throws Exception {
        // given
        String photoId = "456";

        given(photoService.getPhotoDetail(null, photoId)).willReturn(mockPhotoDetailDto);
        // when
        String requestUri = "/api/v1/photos/"+photoId;
        ResultActions actions = mockMvc
                .perform(get(requestUri));

        // then
        actions
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("userId").doesNotExist())

        ;
    }

    @Test
    void detailPhotoByUserId() throws Exception {
        // given
        String photoId = "456";
        String userId = "123";

        given(photoService.getPhotoDetail(userId, photoId)).willReturn(mockPhotoDetailDto);
        // when
        String requestUri = "/api/v1/photos/"+photoId+"?users="+userId;
        ResultActions actions = mockMvc
                .perform(get(requestUri));

        // then
        actions
                .andExpect(status().isOk())
                .andDo(print());
    }

}

