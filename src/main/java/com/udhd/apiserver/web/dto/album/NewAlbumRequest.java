package com.udhd.apiserver.web.dto.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewAlbumRequest {
    /**
     * 저장할 사진의 id
     */
    @NotNull
    private String photoId;
}
