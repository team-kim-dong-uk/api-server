package com.udhd.apiserver.web.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {

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
   * 최애 그룹
   */
  String group;
  /**
   * 업로드한 사진 수
   */
  Integer numUploadedPhotos;
  /**
   * 앨범에 저장한 사진 수
   */
  Integer numAlbumPhotos;
  /**
   * 좋아요한 사진 수
   */
  Integer numLikePhotos;
  /**
   * 앨범에 저장한 사진 수
   */
  Integer numSavePhotos;
}
