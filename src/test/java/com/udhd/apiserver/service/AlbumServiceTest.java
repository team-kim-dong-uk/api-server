package com.udhd.apiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.domain.album.AlbumRepository;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.web.dto.album.AlbumOutlineDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;


@SpringBootTest
public class AlbumServiceTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mock
    AlbumRepository albumRepository;
    @Mock
    PhotoRepository photoRepository;

    @InjectMocks
    AlbumService albumService;

    protected MockMvc mockMvc;

    @Test
    @DisplayName("중복된 사진 앨범 추가")
    void saveAlbum_Duplicate() throws Exception {
        String userId = "60e2fea74c17cf5152fb5b78";
        String photoId = "60e2fea74c17cf5152fb5b78";

        // 이미 가지고 있는 앨범 데이터
        given(albumRepository.findByUserIdAndFeedId(new ObjectId(userId), new ObjectId(photoId)))
                .willThrow(DuplicateKeyException.class);

        assertThrows(DuplicateKeyException.class,
                () -> albumService.saveAlbum(userId, photoId));
    }

}

