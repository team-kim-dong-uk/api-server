package com.udhd.apiserver.web.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    /**
     * user id
     */
    String userId;
    /**
     * 닉네임
     */
    String nickname;
    /**
     * 소셜로그인시 사용한 이메일 주소
     */
    String email;
    /**
     * 업로드한 사진 수
     */
    Integer numUploadedPhotos;
    /**
     * 앨범에 저장한 사진 수
     */
    Integer numAlbumPhotos;
}
