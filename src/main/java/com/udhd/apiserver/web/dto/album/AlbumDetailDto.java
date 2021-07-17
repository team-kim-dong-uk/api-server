package com.udhd.apiserver.web.dto.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDetailDto {
    /**
     * 저장한 사진의 id
     */
    private String albumId;
    /**
     * 사진 업로더 id
     */
    private String uploaderId;
    /**
     * 사진 업로더 닉네임
     */
    private String uploaderNickname;
    /**
     * 해당 사진을 즐겨찾기에 등록한 사람 수
     */
    private Integer favouriteCount;
    /**
     * 원본사진 링크
     */
    private String originalLink;
    /**
     * 즐겨찾기 지정 여부
     */
    private Boolean favourite;
    /**
     * 저장 날짜
     */
    private Date savedAt;
    /**
     * 태그
     */
    private List<String> tags;
}
