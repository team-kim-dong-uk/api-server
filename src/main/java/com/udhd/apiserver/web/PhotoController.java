package com.udhd.apiserver.web;

import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.web.dto.photo.PhotoDetailDto;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.photo.TagListDto;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/photos")
@RestController
public class PhotoController {

  private final PhotoService photoService;

  /**
   * 대량 업로드?????? TODO
   */
  @PostMapping("")
  @ResponseStatus(HttpStatus.CREATED)
  public void uploadPhotos() {
    return;
  }

  @GetMapping("/random")
  public List<PhotoOutlineDto> randomPhotos(
      @RequestParam(defaultValue = "24") int count) {
    return photoService.getRandomPhotos(count);
  }

  /**
   * 사진에 적절한 태그 리스트를 반환한다. 사진 전달방법 정해야함. TODO
   *
   * @return the list
   */
  @PostMapping("/recommend/tags")
  @ResponseStatus(HttpStatus.OK)
  public TagListDto recommendTags() {
    return TagListDto.builder().tags(Arrays.asList("오마이걸", "멤버1", "1집")).build();
  }

  /**
   * 사진의 상세정보를 반환한다.
   *
   * @param photoId the photo id
   * @param userId check to find album (optional)
   * @return the uploaded photo detail response
   */
  @GetMapping("/{photoId}")
  @ResponseStatus(HttpStatus.OK)
  public PhotoDetailDto detailPhoto(
      @PathVariable String photoId,
      @RequestParam(required = false) String userId) {
    return photoService.getPhotoDetail(userId, photoId);
  }
}
