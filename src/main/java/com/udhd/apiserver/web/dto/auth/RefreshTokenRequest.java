package com.udhd.apiserver.web.dto.auth;

import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RefreshTokenRequest {

  /**
   * 기존의 refresh token
   */
  @NotNull
  private String refreshToken;
}
