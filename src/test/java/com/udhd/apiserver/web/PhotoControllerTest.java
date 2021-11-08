package com.udhd.apiserver.web;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.web.dto.photo.PhotoDetailDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Date;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PhotoControllerTest extends ControllerTest{

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

