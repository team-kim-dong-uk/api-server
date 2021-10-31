package com.udhd.apiserver.web.dto.auth;

import com.udhd.apiserver.config.auth.dto.Tokens;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class TokenDto {

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

  public TokenDto(Tokens tokens, String userId) {
    this.accessToken = tokens.getAccessToken();
    this.refreshToken = tokens.getRefreshToken();
    this.userId = userId;
  }
}
