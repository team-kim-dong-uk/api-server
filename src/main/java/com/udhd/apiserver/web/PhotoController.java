package com.udhd.apiserver.web;

import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.search.SearchService;
import com.udhd.apiserver.web.dto.ErrorResponse;
import com.udhd.apiserver.web.dto.photo.PhotoDetailDto;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.photo.TagListDto;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
  private final SearchService searchService;

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

  @GetMapping("/tags")
  public Object getPhotoByTags(
      @RequestParam(defaultValue = "") List<String> tags,
      @RequestParam(defaultValue = "0") Integer page,
      HttpServletResponse response
  ) {
    try {
      return searchService.searchPhotoByAllTags(tags, page);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
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
