package com.udhd.apiserver.web.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
}
