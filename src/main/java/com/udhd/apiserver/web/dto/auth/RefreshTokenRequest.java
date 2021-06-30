package com.udhd.apiserver.web.dto.auth;

import lombok.Getter;

@Getter
public class RefreshTokenRequest {
    private String refreshToken;
}
