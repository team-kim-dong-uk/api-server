package com.udhd.apiserver.web;

import com.udhd.apiserver.domain.upload.Upload;
import com.udhd.apiserver.service.UploadService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.ErrorResponse;
import com.udhd.apiserver.web.dto.GeneralResponse;
import com.udhd.apiserver.web.dto.SuccessResponse;
import com.udhd.apiserver.web.dto.upload.PresignedURLProgressResponse;
import com.udhd.apiserver.web.dto.upload.PresignedURLRequest;
import com.udhd.apiserver.web.dto.upload.PresignedURLResponse;
import com.udhd.apiserver.web.dto.upload.TagUploadRequest;
import com.udhd.apiserver.web.dto.upload.UploadWithGoogleDriveRequest;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
@RestController
@Slf4j
public class UploadController {

  private final UploadService uploadService;

  @PostMapping("/google-drive")
  public String uploadGDrive(
      @RequestBody UploadWithGoogleDriveRequest uploadWithGoogleDriveRequest) {
    String userId = SecurityUtils.getLoginUserId();
    String pollingKey = uploadService.generatePollingKey(userId);
    List<Upload> uploads = uploadService
        .uploadWithGoogleDrive(uploadWithGoogleDriveRequest, pollingKey);
    // 비동기로 업로드 태스크 진행
    uploadService
        .processUploadsFromGDrive(uploads, uploadWithGoogleDriveRequest.getGoogleDriveToken());
    return pollingKey;
  }

  @GetMapping("/progress/{pollingKey}")
  public Long getProgress(@PathVariable String pollingKey) {
    return uploadService.getProgress(pollingKey);
  }

  /**
   * s3 pre-signed url들을 반환한다. 리턴값의 i번째 값은 i번째 사진이 새 사진인 경우 presigned url이고, i번째 사진이 기존에 있던 사진인 경우
   * null 이다.
   *
   * @param presignedURLRequest the presigned url request
   * @return the list
   */
  @RequestMapping("/presigned-url")
  public PresignedURLResponse presignedURLs(@RequestBody PresignedURLRequest presignedURLRequest) {
    String userId = SecurityUtils.getLoginUserId();
    String pollingKey = uploadService.generatePollingKey(userId);
    List<Upload> uploads = uploadService
        .createUpload(userId, pollingKey, presignedURLRequest.getChecksums());
    uploadService.fillPresignedUrl(uploads);
    List<String> photoIds = uploadService.getPhotoIdsByHash(userId, presignedURLRequest.getChecksums());

    PresignedURLResponse res = PresignedURLResponse.builder()
        .pollingKey(pollingKey)
        .checksums(presignedURLRequest.getChecksums())
        .photoIds(photoIds)
        .urls(uploads.stream().map(Upload::getS3Url).collect(Collectors.toList()))
        .build();
    return res;
  }

  @RequestMapping("/resize")
  public void resize() {
    uploadService.createThumbnailsAndScaledImages();
  }

  @RequestMapping("/presigned-url/{pollingKey}/{checksum}")
  public GeneralResponse markProgress(@PathVariable String pollingKey,
      @PathVariable String checksum) {
    PresignedURLProgressResponse retval = new PresignedURLProgressResponse();
    try {
      String photoId = uploadService.confirmUpload(pollingKey, checksum);
      retval.setPhotoId(photoId);
      retval.setProgress(uploadService.getProgress(pollingKey));
    } catch (Exception e) {
      log.info("failed to mark pollingKey : " + pollingKey + " checksum : " + checksum, e);
    }
    return retval;
  }

  @PutMapping("/feed/{feedId}/tags")
  public GeneralResponse uploadTagsByFeedId(
      @PathVariable String feedId,
      @RequestBody TagUploadRequest request,
      HttpServletResponse response
  ) {
    try {
      boolean propagate = request.getPropagate() != null ? request.getPropagate() : false;
      uploadService.setTagsByFeedId(feedId, request.getTags(), propagate);
      SuccessResponse retval = new SuccessResponse();
      retval.setMessage("success");
      return retval;
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }

  @PutMapping("/photo/{photoId}/tags")
  public GeneralResponse uploadTagsByPhotoId(
      @PathVariable String photoId,
      @RequestBody TagUploadRequest request,
      HttpServletResponse response
  ) {
    try {
      uploadService.setTagsByPhotoId(photoId, request.getTags());
      SuccessResponse retval = new SuccessResponse();
      retval.setMessage("success");
      return retval;
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ErrorResponse(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }
}
