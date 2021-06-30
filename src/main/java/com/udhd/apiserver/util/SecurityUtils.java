package com.udhd.apiserver.util;

import com.udhd.apiserver.domain.user.UserInfo;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static UserInfo getLoginUser() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static String getLoginUserId() {
        UserInfo loginUser = getLoginUser();
        return loginUser != null ? loginUser.getId() : "";
    }
}
