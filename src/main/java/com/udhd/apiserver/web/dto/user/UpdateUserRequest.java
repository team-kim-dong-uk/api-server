package com.udhd.apiserver.web.dto.user;

import lombok.Getter;

@Getter
public class UpdateUserRequest {
    /**
     * update 할 닉네임
     */
    private String nickname;
}
