package com.udhd.apiserver.config.auth;

import com.udhd.apiserver.exception.auth.InvalidAccessTokenException;
import com.udhd.apiserver.web.dto.ErrorResponse;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    } catch (InvalidAccessTokenException e) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType("application/json");
      ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
          e.getMessage());
      try {
        String json = errorResponse.toJson();
        response.getWriter().write(json);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
