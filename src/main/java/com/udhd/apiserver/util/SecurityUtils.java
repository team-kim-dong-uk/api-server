package com.udhd.apiserver.util;

import com.udhd.apiserver.domain.user.UserInfo;
import com.udhd.apiserver.exception.auth.NoAuthorityException;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static UserInfo getLoginUser() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static String getLoginUserId() {
        UserInfo loginUser = getLoginUser();
        return loginUser != null ? loginUser.getId() : "";
    }

    /**
     * 현재 로그인한 사용자가 userId가 아니면 NoAuthorityException 을 던진다.
     * @param userId
     * @throws NoAuthorityException
     */
    public static void checkUser(String userId) throws NoAuthorityException {
        if (!userId.equals(getLoginUserId())) {
            throw new NoAuthorityException();
        }
    }
}
