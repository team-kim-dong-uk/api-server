package com.udhd.apiserver.web;

import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.album.AlbumDetailDto;
import com.udhd.apiserver.web.dto.album.AlbumOutlineDto;
import com.udhd.apiserver.web.dto.album.NewAlbumRequest;
import com.udhd.apiserver.web.dto.album.UpdateTagsRequest;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{userId}/album")
@RestController
public class AlbumController {

  private final AlbumService albumService;

  /**
   * 앨범에 새 사진 저장.
   *
   * @param userId the user id
   * @param newAlbumRequest the new album photo request
   * @return the album photo detail response
   */
  @PostMapping("")
  @ResponseStatus(HttpStatus.CREATED)
  public AlbumDetailDto newAlbum(
      @PathVariable String userId,
      @RequestBody @Valid NewAlbumRequest newAlbumRequest) {
    SecurityUtils.checkUser(userId);

    return albumService.saveAlbum(userId, newAlbumRequest.getPhotoId());
  }


  /**
   * 내 앨범 사진 리스트. TODO
   *
   * @param userId the user id
   * @param tags 검색 태그 목록
   * @param sortBy 정렬 기준
   * @param uploadedOnly true이면 내가 업로드한 사진만, false면 업로드/저장한 사진을 모두 보여줌
   * @param findAfter 지난번 검색결과의 가장 마지막 원소의 id. 이 원소 다음부터 찾기 시작한다. null이면 처음부터 찾는다.
   * @param fetchSize the fetch size
   * @return the list
   */
  @GetMapping("")
  @ResponseStatus(HttpStatus.OK)
  public List<AlbumOutlineDto> listAlbum(
      @PathVariable String userId,
      @RequestParam(defaultValue = "") List<String> tags,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "false") Boolean uploadedOnly,
      @RequestParam(required = false) String findAfter,
      @RequestParam(defaultValue = "21") Integer fetchSize) {
    SecurityUtils.checkUser(userId);

    return albumService.findAlbums(userId, tags, findAfter, fetchSize);
  }

  /**
   * 앨범 태그 정보를 업데이트한다. 내가 업로드한 사진일 경우, 태그를 수정하면 다른 사용자들의 검색결과에 반영된다.
   *
   * @param userId the user id
   * @param albumId the album id
   * @param updateTagsRequest the update album tags request
   * @return the album photo detail response
   */
  @PatchMapping("/{albumId}/tags")
  @ResponseStatus(HttpStatus.OK)
  public AlbumDetailDto updateAlbumTags(
      @PathVariable String userId,
      @PathVariable String albumId,
      @RequestBody @Valid UpdateTagsRequest updateTagsRequest) {
    SecurityUtils.checkUser(userId);

    return albumService.updateAlbumTags(userId, albumId, updateTagsRequest.getTags());
  }

  /**
   * 앨범 사진을 삭제한다.
   *
   * @param userId the user id
   * @param albumId the album id
   */
  @DeleteMapping("/{albumId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAlbum(@PathVariable String userId, @PathVariable String albumId) {
    SecurityUtils.checkUser(userId);

    albumService.deleteAlbum(userId, albumId);
  }


}
