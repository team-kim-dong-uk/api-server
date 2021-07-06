package com.udhd.apiserver.config.auth;

import com.udhd.apiserver.config.auth.dto.Tokens;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.service.AuthService;
import com.udhd.apiserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final AuthService authService;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String targetUrl = generateTargetUrlWithTokens(oAuth2User);

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String generateTargetUrlWithTokens(CustomOAuth2User oAuth2User) {
        String email = oAuth2User.getEmail();
        Optional<User> foundUser = userService.findByEmail(email);
        User user = foundUser.orElseGet(()->{
                                User newUser = User.builder()
                                                    .email(email)
                                                    .nickname("어덕행덕")
                                                    .build();
                                return userService.insert(newUser);
                            });
        Tokens tokens = authService.issueRefreshToken(user.getId());

        return UriComponentsBuilder
                .fromUriString("/api/v1/auth/redirect-page")
                .queryParam("accessToken", tokens.getAccessToken())
                .queryParam("refreshToken", tokens.getRefreshToken())
                .build().toUriString();
    }
}
