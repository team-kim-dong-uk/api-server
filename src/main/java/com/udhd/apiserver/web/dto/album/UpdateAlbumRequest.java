package com.udhd.apiserver.web.dto.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAlbumRequest {
    /**
     * 즐겨찾기 지정 여부
     */
    private Boolean favourite;
    /**
     * 새로운 태그들. 수정된 태그들만이 아닌 모든 태그를 전달해주어야 한다
     */
    private List<String> tags;
}
