package com.udhd.apiserver.integration;

import com.udhd.apiserver.config.auth.dto.TokenInfo;
import com.udhd.apiserver.config.auth.dto.Tokens;
import com.udhd.apiserver.exception.auth.InvalidRefreshTokenException;
import com.udhd.apiserver.integration.IntegrationTest;
import com.udhd.apiserver.service.AuthService;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class AuthIntegrationTest extends IntegrationTest {

    @MockBean
    private AuthService authService;

    @Test
    void reissueRefreshToken() throws Exception {
        // given
        String refreshToken = "<valid-refresh-token>";
        String refreshTokenRequest = "{\"refreshToken\" : \""+refreshToken+"\"}";
        Tokens generatedTokens = Tokens.builder().accessToken("<access-token>").refreshToken("<refresh-token>").build();
        TokenInfo validTokenInfo = TokenInfo.builder()
                                                .userId(userId)
                                                .build();

        given(authService.validateRefreshToken(refreshToken)).willReturn(validTokenInfo);
        given(authService.issueRefreshToken(any())).willReturn(generatedTokens);

        // when
        ResultActions actions = mockMvc
                .perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenRequest));

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("&lt;access-token&gt;")))
                .andExpect(jsonPath("$.refreshToken", is("&lt;refresh-token&gt;")));
    }

    @Test
    void reissueRefreshTokenExpired() throws Exception {
        // given
        String expiredToken = "<expired_refresh_token>";
        String refreshTokenRequest = "{\"refreshToken\":\""+expiredToken+"\"}";

        given(authService.validateRefreshToken(expiredToken))
                .willThrow(new InvalidRefreshTokenException("Expired refresh token"));

        // when
        ResultActions actions = mockMvc
                .perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenRequest));

        // then
        actions
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reissueRefreshTokenInvalid() throws Exception {
        // given
        String invalidToken = "<invalid_refresh_token>";
        String refreshTokenRequest = "{\"refreshToken\":\""+invalidToken+"\"}";

        given(authService.validateRefreshToken(invalidToken))
                .willThrow(new InvalidRefreshTokenException("Invalid refresh token"));

        // when
        ResultActions actions = mockMvc
                .perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenRequest));

        // then
        actions
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reissueRefreshTokenNoToken() throws Exception {
        // given
        String refreshTokenRequest = "{\"refreshToken\" : null}";

        given(authService.validateRefreshToken(null))
                .willThrow(new InvalidRefreshTokenException("No refresh token"));

        // when
        ResultActions actions = mockMvc
                .perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenRequest));

        // then
        actions
                .andExpect(status().isUnauthorized());
    }
}
