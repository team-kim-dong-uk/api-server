package com.udhd.apiserver.config.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.udhd.apiserver.config.auth.dto.TokenInfo;
import com.udhd.apiserver.domain.user.UserInfo;
import com.udhd.apiserver.exception.auth.InvalidAccessTokenException;
import com.udhd.apiserver.util.JwtUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;
  private final String[] allowedPaths;

  public JwtAuthenticationFilter(JwtUtils jwtUtils, String[] allowedPaths) {
    this.jwtUtils = jwtUtils;
    this.allowedPaths = allowedPaths;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    boolean allowed = false;
    // jwt 인증이 필요없는 경로라면, 통과시켜준다.
    // allowedPaths는 SecurityConfig에서 해당 filter를 생성할 때 인자로 넣어준다.
    for (String allowedPath : allowedPaths) {
      if (request.getRequestURI().startsWith(allowedPath)) {
        allowed = true;
      }
    }
    // Authorization 헤더를 읽어와 jwt 인증 진행
    String authorizationHeader = request.getHeader("Authorization");
    if (!isAuthorizationHeaderValid(authorizationHeader)) {
      if (allowed) {
        filterChain.doFilter(request, response);
      } else {
        throw new InvalidAccessTokenException("No Access Token");
      }
    }
    try {
      UsernamePasswordAuthenticationToken token = createToken(authorizationHeader);
      SecurityContextHolder.getContext().setAuthentication(token);
      filterChain.doFilter(request, response);
    } catch (JWTVerificationException | IllegalArgumentException e) {
      if (allowed) {
        filterChain.doFilter(request, response);
      } else {
        throw new InvalidAccessTokenException("Invalid Access Token");
      }
    }
  }

  private boolean isAuthorizationHeaderValid(String authorizationHeader) {
    return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
  }

  private UsernamePasswordAuthenticationToken createToken(String authorizationHeader)
      throws JWTVerificationException, IllegalArgumentException {
    String token = authorizationHeader.replace("Bearer ", "");
    TokenInfo tokenInfo = jwtUtils.parseToken(token);
    UserInfo userInfo = UserInfo.builder().id(tokenInfo.getUserId()).build();
    List<GrantedAuthority> authorities = new ArrayList<>();
    return new UsernamePasswordAuthenticationToken(userInfo, null, authorities);
  }

}
