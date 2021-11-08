package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.album.AlbumRepository;
import com.udhd.apiserver.domain.feed.FeedRepository;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AlbumServiceTest {

    @Mock
    AlbumRepository albumRepository;
    @Mock
    PhotoRepository photoRepository;
    @Mock
    FeedRepository feedRepository;

    @InjectMocks
    AlbumService albumService;

    @BeforeEach
    void setUp() {
        this.albumService = new AlbumService(albumRepository, photoRepository, feedRepository);
    }

    @Test
    @DisplayName("중복된 사진 앨범 추가")
    void saveAlbum_Duplicate() throws Exception {
        String userId = "60e2fea74c17cf5152fb5b78";
        String photoId = "60e2fea74c17cf5152fb5b78";

        // 이미 가지고 있는 앨범 데이터
        when(albumRepository.findByUserIdAndFeedId(new ObjectId(userId), new ObjectId(photoId)))
                .thenThrow(DuplicateKeyException.class);
        /*given(albumRepository.findByUserIdAndFeedId(new ObjectId(userId), new ObjectId(photoId)))
                .willThrow(DuplicateKeyException.class);*/

        assertThrows(DuplicateKeyException.class,
                () -> albumService.saveAlbum(userId, photoId));
    }

}

