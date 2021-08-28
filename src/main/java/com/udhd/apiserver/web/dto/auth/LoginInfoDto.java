package com.udhd.apiserver.web.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

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
     * 처음 가입하는 유저인지 여부
     */
    private boolean isNewUser;
}
