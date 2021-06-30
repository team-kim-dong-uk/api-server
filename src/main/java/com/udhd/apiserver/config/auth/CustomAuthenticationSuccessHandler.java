package com.udhd.apiserver.config.auth;

import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
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
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String targetUrl = generateTargetUrlWithTokens(oAuth2User);

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String generateTargetUrlWithTokens(DefaultOAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        Optional<User> foundUser = userService.findByEmail(email);
        User user = foundUser.orElseGet(()->{
                                User newUser = User.builder()
                                                    .email(email)
                                                    .nickname("어덕행덕")
                                                    .build();
                                return userService.insert(newUser);
                            });

        String token = jwtUtils.generateAccessToken(user.getId());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());
        user.setRefreshToken(refreshToken);
        userService.save(user);

        return UriComponentsBuilder
                .fromUriString("/api/v1/auth/redirect-page")
                .queryParam("token", token)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
    }
}
