package com.udhd.apiserver.web;

import com.udhd.apiserver.domain.upload.Upload;
import com.udhd.apiserver.service.UploadService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.upload.PresignedURLRequest;
import com.udhd.apiserver.web.dto.upload.PresignedURLResponse;
import com.udhd.apiserver.web.dto.upload.UploadWithGoogleDriveRequest;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
@RestController
@Slf4j
public class UploadController {
    private final UploadService uploadService;

    @PostMapping("/google-drive")
    public String uploadGDrive(@RequestBody UploadWithGoogleDriveRequest uploadWithGoogleDriveRequest) {
        String userId = SecurityUtils.getLoginUserId();
        String pollingKey = uploadService.generatePollingKey(userId);
        List<Upload> uploads = uploadService.uploadWithGoogleDrive(uploadWithGoogleDriveRequest, pollingKey);
        // 비동기로 업로드 태스크 진행
        uploadService.processUploadsFromGDrive(uploads, uploadWithGoogleDriveRequest.getGoogleDriveToken());
        return pollingKey;
    }

    @GetMapping("/progress/{pollingKey}")
    public Long getProgress(@PathVariable String pollingKey) {
        return uploadService.getProgress(pollingKey);
    }

    /**
     * s3 pre-signed url들을 반환한다.
     * 리턴값의 i번째 값은
     *   i번째 사진이 새 사진인 경우 presigned url이고,
     *   i번째 사진이 기존에 있던 사진인 경우 null 이다.
     *
     * @param presignedURLRequest the presigned url request
     * @return the list
     */
    @RequestMapping("/presigned-url")
    public PresignedURLResponse presignedURLs(@RequestBody PresignedURLRequest presignedURLRequest) {
        String userId = SecurityUtils.getLoginUserId();
        String pollingKey = uploadService.generatePollingKey(userId);
        List<Upload> uploads = uploadService.createUpload(userId, pollingKey, presignedURLRequest.getChecksums());
        uploadService.fillPresignedUrl(uploads);

        PresignedURLResponse res = PresignedURLResponse.builder()
            .pollingKey(pollingKey)
            .checksums(presignedURLRequest.getChecksums())
            .urls(uploads.stream().map(Upload::getChecksum).collect(Collectors.toList()))
            .build();
        return res;
    }

    @RequestMapping("/presigned-url/{pollingKey}/{checksum}")
    public void markProgress(@PathVariable String pollingKey,
        @PathVariable String checksum) {
      // TODO: Album 에 넣어줘야함. GDrive에서 업로드할때는 타는 로직인데, directly upload 에서는 아직 구성 안함
        boolean success = uploadService.markCompleted(pollingKey, checksum);
        if (!success)
            log.info("failed to mark pollingKey : " + pollingKey + " checksum : "+ checksum);
    }
}
