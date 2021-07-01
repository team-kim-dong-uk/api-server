package com.udhd.apiserver.web.dto.auth;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class RefreshTokenRequest {
    /**
     * 기존의 refresh token
     */
    @NotNull
    private String refreshToken;
}
