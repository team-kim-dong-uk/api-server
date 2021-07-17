package com.udhd.apiserver.web.dto.photo;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@Builder
public class PhotoDetailDto {
    /**
     * 사진의 id
     */
    private String photoId;
    /**
     * 사진 업로더 아이디
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
     * 업로드 날짜
     */
    private Date uploadedAt;
    /**
     * 태그
     */
    private List<String> tags;
}
