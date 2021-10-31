package com.udhd.apiserver.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.udhd.apiserver.config.auth.CustomOAuth2User;
import com.udhd.apiserver.config.auth.dto.TokenInfo;
import com.udhd.apiserver.config.auth.dto.Tokens;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.domain.user.UserRepository;
import com.udhd.apiserver.exception.auth.InvalidRefreshTokenException;
import com.udhd.apiserver.util.JwtUtils;
import com.udhd.apiserver.web.dto.auth.LoginInfoDto;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * The type Auth service.
 */
@RequiredArgsConstructor
@Service
public class AuthService {

  private final JwtUtils jwtUtils;
  private final UserRepository userRepository;

  /**
   * authentication 정보를 가지고 loginInfoDto 객체를 만든다. authentication.principle의 email을 기준으로, DB에 없는 유저면
   * 새로 추가한 후 진행한다
   *
   * @return login info
   */
  public LoginInfoDto generateLoginInfo(Authentication authentication) {
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getEmail();
    Optional<User> foundUser = userRepository.findByEmail(email);

    boolean isNewUser = foundUser.isEmpty();
    User user = foundUser.orElseGet(() -> {
      User newUser = User.builder()
          .email(email)
          .build();
      return userRepository.insert(newUser);
    });

    return toLoginInfoDto(user, isNewUser, oAuth2User.getGoogleToken());
  }

  /**
   * Validate refresh token token info.
   *
   * 아래의 경우에 InvalidRefreshTokenException 을 throw 한다. 1. refresh token data 가 올바르게 decode 되지 않는 경우
   * 2. refresh token 이 expired 된 경우 3. refresh token 이 DB에 있는 값과 다른 경우
   *
   * @param refreshToken the refresh token
   * @return the token info
   * @throws InvalidRefreshTokenException the invalid refresh token exception
   */
  public TokenInfo validateRefreshToken(String refreshToken) throws InvalidRefreshTokenException {
    // reject if no refresh token
    if (refreshToken == null) {
      throw new InvalidRefreshTokenException("No refresh token");
    }
    // validate token if refresh token is given
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
   * userId 용 access token, refresh token 을 발급하여 tokens 객체에 담아 리턴한다. 해당 userId 가 DB에 없는 유저일 경우
   * InvalidRefreshTokenException 을 throw 한다.
   *
   * @param userId the user id
   * @return the tokens (access token, refresh token)
   * @throws InvalidRefreshTokenException the invalid refresh token exception
   */
  public Tokens issueRefreshToken(ObjectId userId) throws InvalidRefreshTokenException {
    String newAccessToken = jwtUtils.generateAccessToken(userId);
    String newRefreshToken = jwtUtils.generateRefreshToken(userId);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
    user.setRefreshToken(newRefreshToken);
    userRepository.save(user);

    return Tokens.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();
  }

  private LoginInfoDto toLoginInfoDto(User user, boolean isNewUser, String googleToken) {
    ObjectId userId = user.getId();
    Tokens tokens = issueRefreshToken(userId);

    return LoginInfoDto.builder()
        .userId(userId.toString())
        .accessToken(tokens.getAccessToken())
        .refreshToken(tokens.getRefreshToken())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .group(user.getGroup())
        .googleToken(googleToken)
        .isNewUser(isNewUser)
        .build();
  }
}
