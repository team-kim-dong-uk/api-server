package com.udhd.apiserver.web.dto.auth;

import com.udhd.apiserver.config.auth.dto.Tokens;
import lombok.Getter;

@Getter
public class TokenResponse {
    /**
     * 새로 발급받은 access token
     */
    private String accessToken;
    /**
     * 새로 발급받은 refresh token
     */
    private String refreshToken;

    public TokenResponse(Tokens tokens) {
        this.accessToken = tokens.getAccessToken();
        this.refreshToken = tokens.getRefreshToken();
    }
}
