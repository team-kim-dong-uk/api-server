package com.udhd.apiserver.config.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class TokenInfo {
    private String userId;
    private Date expiresAt;
}
