package com.udhd.apiserver.web;

import com.udhd.apiserver.domain.upload.Upload;
import com.udhd.apiserver.service.UploadService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.upload.PresignedURLRequest;
import com.udhd.apiserver.web.dto.upload.UploadWithGoogleDriveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
@RestController
public class UploadController {
    private final UploadService uploadService;

    @PostMapping("/google-drive")
    public String upload(@RequestBody UploadWithGoogleDriveRequest uploadWithGoogleDriveRequest) {
        String userId = SecurityUtils.getLoginUserId();
        String pollingKey = uploadService.generatePollingKey(userId);
        List<Upload> uploads = uploadService.uploadWithGoogleDrive(uploadWithGoogleDriveRequest, pollingKey);
        // 비동기로 업로드 태스크 진행
        uploadService.processUploads(uploads, uploadWithGoogleDriveRequest.getGoogleDriveToken());
        return pollingKey;
    }

    @GetMapping("/progress/{pollingKey}")
    public Long checkProgress(@PathVariable String pollingKey) {
        return uploadService.checkProgress(pollingKey);
    }

}
