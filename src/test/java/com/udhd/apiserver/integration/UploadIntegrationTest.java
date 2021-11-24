package com.udhd.apiserver.integration;

import com.udhd.apiserver.domain.upload.Upload;
import com.udhd.apiserver.integration.IntegrationTest;
import com.udhd.apiserver.service.UploadService;
import com.udhd.apiserver.util.JsonUtils;
import com.udhd.apiserver.web.dto.upload.PresignedURLResponse;
import java.util.HashMap;
import java.util.Map;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class UploadIntegrationTest extends IntegrationTest {

    @MockBean
    private UploadService uploadService;

    @Test
    void presignedUrls() throws Exception {
        // given
        String userId = "60e2fea74c17cf5152fb5b78";
        List<String> checksums = Arrays.asList(
            "912ec803b2ce49e4a541068d495ab570",
            "6a204bd89f3c8348afd5c77c717a097a");
        List<String> resultUrls = Arrays.asList("http://example-url", null);
        Map<String, Object> data = new HashMap<>();
        data.put("checksums", checksums);
        String presignedUrlRequest = JsonUtils.getInstance().stringify(data);

        String dummyPollingKey = uploadService.generatePollingKey(userId);

        PresignedURLResponse dummyRes = PresignedURLResponse.builder()
            .pollingKey(uploadService.generatePollingKey(userId))
            .checksums(checksums) /* Dummy md5 value */
            .urls(resultUrls)
            .build();

            List<Upload> dummyUploads = Arrays.asList(
                Upload.builder().pollingKey(dummyPollingKey)
                .checksum(checksums.get(0))
                .s3Url(resultUrls.get(0))
                .uploaderId(new ObjectId(userId))
                .build(),
                Upload.builder().pollingKey(dummyPollingKey)
                    .checksum(checksums.get(1))
                    .s3Url(resultUrls.get(1))
                    .uploaderId(new ObjectId(userId))
                    .build()
            );
        given(uploadService.createUpload(any(), any(), any()))
            .willReturn(dummyUploads);

        // when
        String requestUri = "/api/v1/upload/presigned-url";
        ResultActions actions = mockMvc
                .perform(post(requestUri).with(userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(presignedUrlRequest));

        // then
        actions
                .andExpect(status().isOk());
    }

}
