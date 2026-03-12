package com.ecommerce.auth_service.filters;

import com.ecommerce.auth_service.token.JwtAuthenticationToken;
import com.ecommerce.auth_service.utils.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtValidationFilter extends OncePerRequestFilter {

  private final AuthenticationManager authenticationManager;
  private final JWTUtil jwtUtil;

  public JwtValidationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String token = null;
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      token = bearerToken.substring(7);
    }

    if (token != null) {
      if (!jwtUtil.isTokenType(token, JWTUtil.TOKEN_TYPE_ACCESS)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(token);
      Authentication authentication = authenticationManager.authenticate(jwtAuthenticationToken);
      if (authentication.isAuthenticated()) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }
}
