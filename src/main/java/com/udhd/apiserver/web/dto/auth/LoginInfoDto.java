package com.udhd.apiserver.web.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class LoginInfoDto {

  /**
   * 새로 발급받은 access token
   */
  private String accessToken;
  /**
   * 새로 발급받은 refresh token
   */
  private String refreshToken;

  /**
   * userId
   */
  private String userId;

  /**
   * email
   */
  private String email;

  /**
   * nickname
   */
  private String nickname;

  /**
   * 최애 그룹
   */
  private String group;

  /**
   * googleDrive용 액세스 토큰
   */
  private String googleToken;

  /**
   * 처음 가입하는 유저인지 여부
   */
  private boolean isNewUser;
}
