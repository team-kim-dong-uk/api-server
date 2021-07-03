package com.udhd.apiserver.web.dto.album;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlbumOutlineResponse {
    /**
     * 저장된 사진의 id
     */
    private String albumId;
    /**
     * 미리보기용 저화질/용량 이미지 링크
     */
    private String smallLink;
}
