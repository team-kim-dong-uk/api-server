package com.udhd.apiserver.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.udhd.apiserver.config.auth.dto.TokenInfo;
import com.udhd.apiserver.config.auth.dto.Tokens;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.exception.auth.InvalidRefreshTokenException;
import com.udhd.apiserver.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * The type Auth service.
 */
@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;


    /**
     * Validate refresh token token info.
     *
     * 아래의 경우에 InvalidRefreshTokenException 을 throw 한다.
     * 1. refresh token data 가 올바르게 decode 되지 않는 경우
     * 2. refresh token 이 expired 된 경우
     * 3. refresh token 이 DB에 있는 값과 다른 경우
     * @param refreshToken the refresh token
     * @return the token info
     * @throws InvalidRefreshTokenException the invalid refresh token exception
     */
    public TokenInfo validateRefreshToken(String refreshToken) throws InvalidRefreshTokenException {
        TokenInfo tokenInfo;
        try {
            tokenInfo = jwtUtils.parseToken(refreshToken);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            // reject if token data is invalid
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        // reject if refresh token is expired
        if (tokenInfo.getExpiresAt().before(new Date(System.currentTimeMillis()))) {
            throw new InvalidRefreshTokenException("Expired refresh token");
        }

        ObjectId userId = new ObjectId(tokenInfo.getUserId());
        Optional<User> user = userRepository.findById(userId);
        // reject if refresh token is different with DB
        if (user.isEmpty() || !user.get().getRefreshToken().equals(refreshToken)) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        return tokenInfo;
    }

    /**
     * Issue refresh token tokens.
     *
     * userId 용 access token, refresh token 을 발급하여 tokens 객체에 담아 리턴한다.
     * 해당 userId 가 DB에 없는 유저일 경우 InvalidRefreshTokenException 을 throw 한다.
     * @param userId the user id
     * @return the tokens (access token, refresh token)
     * @throws InvalidRefreshTokenException the invalid refresh token exception
     */
    public Tokens issueRefreshToken(ObjectId userId) throws InvalidRefreshTokenException {
        String newAccessToken = jwtUtils.generateAccessToken(userId);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId);
        User user = userRepository.findById(userId)
                                    .orElseThrow(()-> new InvalidRefreshTokenException("Invalid refresh token"));
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return Tokens.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
