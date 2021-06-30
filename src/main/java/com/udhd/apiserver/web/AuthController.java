package com.udhd.apiserver.web;

import com.udhd.apiserver.config.auth.dto.TokenInfo;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.exception.auth.InvalidRefreshTokenException;
import com.udhd.apiserver.service.UserService;
import com.udhd.apiserver.util.JwtUtils;
import com.udhd.apiserver.web.dto.ErrorResponse;
import com.udhd.apiserver.web.dto.auth.RefreshTokenRequest;
import com.udhd.apiserver.web.dto.auth.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@RestController
public class AuthController {
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @PostMapping("/refresh-token")
    public RefreshTokenResponse refreshToken(
            @RequestBody RefreshTokenRequest refreshTokenRequest) throws InvalidRefreshTokenException {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        TokenInfo tokenInfo;
        try {
            tokenInfo = jwtUtils.parseToken(refreshToken);
        } catch (Exception e) {
            // reject if token data is invalid
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        // reject if refresh token is expired
        if (tokenInfo.getExpiresAt().before(new Date(System.currentTimeMillis()))) {
            throw new InvalidRefreshTokenException("Expired refresh token");
        }

        ObjectId userId = new ObjectId(tokenInfo.getUserId());
        Optional<User> user = userService.findById(userId);
        // reject if refresh token is different with DB
        if (user.isEmpty() || !user.get().getRefreshToken().equals(refreshToken)) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        // since refresh token is valid, return new tokens to user
        String newAccessToken = jwtUtils.generateAccessToken(userId);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId);
        User user1 = user.get();
        user1.setRefreshToken(newRefreshToken);
        userService.save(user1);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse invalidRefreshToken(Exception e) {
        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }
}
