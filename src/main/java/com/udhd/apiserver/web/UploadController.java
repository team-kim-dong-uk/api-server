package com.udhd.apiserver.web;

import com.udhd.apiserver.service.UploadService;
import com.udhd.apiserver.web.dto.upload.PresignedURLRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/upload")
@RestController
public class UploadController {
    private final UploadService uploadService;

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
    public List<String> presignedURLs(@RequestBody PresignedURLRequest presignedURLRequest) {
        return uploadService.getPreSignedURLs(presignedURLRequest.getChecksums());
    }
}
