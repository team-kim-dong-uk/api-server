package com.udhd.apiserver.web;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.SnippetRegistry;
import capital.scalable.restdocs.jackson.JacksonResultHandlers;
import capital.scalable.restdocs.response.ResponseModifyingPreprocessors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udhd.apiserver.config.auth.dto.TokenInfo;
import com.udhd.apiserver.config.auth.dto.Tokens;
import com.udhd.apiserver.exception.auth.InvalidRefreshTokenException;
import com.udhd.apiserver.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
@SpringBootTest
public class AuthControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    protected MockMvc mockMvc;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) throws Exception {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(JacksonResultHandlers.prepareJackson(objectMapper))
                .alwaysDo(MockMvcRestDocumentation.document("{class-name}/{method-name}",
                        Preprocessors.preprocessRequest(),
                        Preprocessors.preprocessResponse(
                                ResponseModifyingPreprocessors.replaceBinaryContent(),
                                ResponseModifyingPreprocessors.limitJsonArrayLength(objectMapper),
                                Preprocessors.prettyPrint())))
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                        .uris()
                        .withScheme("https")
                        .withHost("udhd.djbaek.com")
                        .and().snippets()
                        .withDefaults(CliDocumentation.curlRequest(),
                                HttpDocumentation.httpRequest(),
                                HttpDocumentation.httpResponse(),
                                AutoDocumentation.requestFields(),
                                AutoDocumentation.responseFields(),
                                AutoDocumentation.pathParameters(),
                                AutoDocumentation.requestParameters(),
                                AutoDocumentation.description(),
                                AutoDocumentation.methodAndPath(),
                                AutoDocumentation.sectionBuilder()
                                        .snippetNames(
                                                SnippetRegistry.AUTO_PATH_PARAMETERS,
                                                SnippetRegistry.AUTO_REQUEST_PARAMETERS,
                                                SnippetRegistry.AUTO_REQUEST_FIELDS,
                                                SnippetRegistry.HTTP_REQUEST,
                                                SnippetRegistry.AUTO_RESPONSE_FIELDS,
                                                SnippetRegistry.HTTP_RESPONSE)
                                        .skipEmpty(true)
                                        .build()))
                .build();
    }

    @Test
    void reissueRefreshToken() throws Exception {
        // given
        String refreshToken = "<valid-refresh-token>";
        String refreshTokenRequest = "{\"refreshToken\" : \""+refreshToken+"\"}";
        Tokens generatedTokens = Tokens.builder().accessToken("<access-token>").refreshToken("<refresh-token>").build();
        TokenInfo validTokenInfo = TokenInfo.builder()
                                                .userId("012345678901234567890123")
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
                .andExpect(jsonPath("$.accessToken", is("<access-token>")))
                .andExpect(jsonPath("$.refreshToken", is("<refresh-token>")));
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
