package com.udhd.apiserver.config.auth;

import com.udhd.apiserver.service.AuthService;
import com.udhd.apiserver.web.dto.auth.LoginInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Value("${front.url}")
    private String frontUrl;

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        LoginInfoDto loginInfoDto = authService.generateLoginInfo(authentication);

        getRedirectStrategy().sendRedirect(request, response,
                UriComponentsBuilder
                        .fromHttpUrl(frontUrl + "/login-redirect")
                        .queryParam("userId", loginInfoDto.getUserId())
                        .queryParam("accessToken", loginInfoDto.getAccessToken())
                        .queryParam("refreshToken", loginInfoDto.getRefreshToken())
                        .queryParam("nickname", loginInfoDto.getNickname())
                        .queryParam("isNewUser", loginInfoDto.isNewUser())
                        .queryParam("email", loginInfoDto.getEmail())
                        .build().toUriString()
                );
    }
}
