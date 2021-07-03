package com.udhd.apiserver.web;

import com.udhd.apiserver.web.dto.photo.PhotoDetailResponse;
import com.udhd.apiserver.web.dto.photo.TagListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/photos")
@RestController
public class PhotoController {
    private final PhotoDetailResponse mockPhotoDetailResponse
            = PhotoDetailResponse.builder()
            .photoId("456")
            .uploaderNickname("업로더")
            .originalLink("http://link.com/456")
            .favouriteCount(532)
            .uploadedAt(new Date())
            .tags(Arrays.asList("더보이즈", "멤버1", "1집", "210701"))
            .build();

    /**
     * 대량 업로드?????? TODO
     *
     * @return
     */
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadPhotos() {
        return;
    }

    /**
     * 사진에 적절한 태그 리스트를 반환한다. 사진 전달방법 정해야함. TODO
     *
     * @return the list
     */
    @PostMapping("/recommend/tags")
    @ResponseStatus(HttpStatus.OK)
    public TagListResponse recommendTags() {
        return TagListResponse.builder().tags(Arrays.asList("더보이즈", "멤버1", "1집")).build();
    }

    /**
     *  사진의 상세정보를 반환한다. TODO
     *
     * @param photoId the photo id
     * @return the uploaded photo detail response
     */
    @GetMapping("/{photoId}")
    @ResponseStatus(HttpStatus.OK)
    public PhotoDetailResponse detailPhoto(
            @PathVariable String photoId) {
        return mockPhotoDetailResponse;
    }
}
