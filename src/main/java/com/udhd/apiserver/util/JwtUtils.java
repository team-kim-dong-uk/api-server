package com.udhd.apiserver.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.udhd.apiserver.config.auth.dto.TokenInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;
    public final static long ACCESS_TOKEN_EXPIRE_SECOND = 1000L * 180 * 60;
    public final static long REFRESH_TOKEN_EXPIRE_SECOND = 1000L * 60 * 24 * 30;

    public TokenInfo parseToken(String token)
            throws JWTVerificationException, IllegalArgumentException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(token);
        String userId = jwt.getSubject();
        Date expiresAt = jwt.getExpiresAt();
        return TokenInfo.builder().userId(userId).expiresAt(expiresAt).build();
    }

    public String generateAccessToken(ObjectId userId) {
        return generateToken(userId, ACCESS_TOKEN_EXPIRE_SECOND);
    }

    public String generateRefreshToken(ObjectId userId) {
        return generateToken(userId, REFRESH_TOKEN_EXPIRE_SECOND);
    }

    private String generateToken(ObjectId userId, long expireTime) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String token = JWT.create()
                            .withSubject(userId.toString())
                            .withExpiresAt(new Date(System.currentTimeMillis() + expireTime))
                            .sign(algorithm);
        return token;
    }

}
