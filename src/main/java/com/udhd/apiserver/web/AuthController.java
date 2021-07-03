package com.udhd.apiserver.web;

import com.udhd.apiserver.config.auth.dto.TokenInfo;
import com.udhd.apiserver.exception.auth.InvalidRefreshTokenException;
import com.udhd.apiserver.service.AuthService;
import com.udhd.apiserver.util.JwtUtils;
import com.udhd.apiserver.web.dto.ErrorResponse;
import com.udhd.apiserver.web.dto.auth.RefreshTokenRequest;
import com.udhd.apiserver.web.dto.auth.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {
    private final AuthService authService;

    /**
     * refresh token을 받아서 새로운 access token과 refresh token을 발급해준다.
     *
     * @param refreshTokenRequest the refresh token request
     * @return the token response
     * @throws InvalidRefreshTokenException the invalid refresh token exception
     */
    @PostMapping("/refresh-token")
    public TokenResponse reissueRefreshToken(
            @RequestBody @Valid RefreshTokenRequest refreshTokenRequest) throws InvalidRefreshTokenException {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        TokenInfo tokenInfo = authService.validateRefreshToken(refreshToken);
        ObjectId userId = new ObjectId(tokenInfo.getUserId());
        return new TokenResponse(authService.issueRefreshToken(userId));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse invalidRefreshToken(Exception e) {
        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }
}
