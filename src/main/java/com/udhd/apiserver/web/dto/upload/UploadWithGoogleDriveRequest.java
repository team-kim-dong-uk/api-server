package com.udhd.apiserver.web.dto.upload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UploadWithGoogleDriveRequest {
    private String googleDriveToken;
    private List<String> fileIds;
}
