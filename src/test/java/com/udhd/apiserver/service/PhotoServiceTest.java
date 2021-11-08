package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.photo.PhotoRepository;
import com.udhd.apiserver.exception.album.AlbumNotFoundException;
import com.udhd.apiserver.web.dto.photo.PhotoDetailDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;



@ExtendWith(MockitoExtension.class)
public class PhotoServiceTest {
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    AlbumService albumService;

    @InjectMocks
    PhotoService photoService;


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
    void detailPhotoByUserId() {
        //PhotoService photoService = new PhotoService(albumService, photoRepository);
        // given
        String photoId = "6110066423a94f0000000000";
        String photoId_A = "6110066423a94f0000000001";
        String photoId_B = "6110066423a94f2222222222";

        String userId = "6110066423a94f1111111111";

        List<Album> albums = Arrays.asList(
                Album.builder()
                        .id(new ObjectId(userId))
                        .feedId(new ObjectId(photoId))
                        .tags(Arrays.asList("오마이걸", "1집", "아이스크림"))
                        .build(),
                Album.builder()
                        .id(new ObjectId(userId))
                        .feedId(new ObjectId(photoId_A))
                        .tags(Arrays.asList("오마이걸", "1집", "내 태그 뀨잉"))
                        .build()
        );
        Photo photo = Photo.builder()
                .id(new ObjectId(photoId))
                .uploaderId(new ObjectId("6110066423a94f2222222222"))
                .tags(Arrays.asList("오마이걸", "멤버1", "1집", "210701"))
                .build();

        given(albumService.getAlbumDetail(userId, photoId)).willReturn(albums.get(0));

        given(photoRepository.findById(new ObjectId(photoId))).willReturn(Optional.ofNullable(photo));
        PhotoDetailDto photoDetailDto = photoService.getPhotoDetail(userId, photoId);
        assertThat(photoDetailDto.isInAlbum()).isTrue();

        given(photoRepository.findById(new ObjectId(photoId_B))).willReturn(Optional.ofNullable(photo));
        given(albumService.getAlbumDetail(userId,photoId_B )).willThrow(new AlbumNotFoundException(new ObjectId(photoId_B)));
        photoDetailDto = photoService.getPhotoDetail(userId, photoId_B);
        assertThat(photoDetailDto.getPhotoId()).isEqualTo(photo.getId().toString());

    }

}

