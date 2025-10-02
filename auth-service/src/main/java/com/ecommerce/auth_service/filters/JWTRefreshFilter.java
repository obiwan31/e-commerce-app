package com.ecommerce.auth_service.filters;

import com.ecommerce.auth_service.entity.AuthUser;
import com.ecommerce.auth_service.token.JwtAuthenticationToken;
import com.ecommerce.auth_service.utils.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

public class JWTRefreshFilter extends OncePerRequestFilter {

  private final AuthenticationManager authenticationManager;
  private final JWTUtil jwtUtil;

  public JWTRefreshFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (!request.getServletPath().equals("/auth/refresh-token")) {
      filterChain.doFilter(request, response);
      return;
    }

    String refreshToken = extractJwtFromRequest(request);
    if (refreshToken == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(refreshToken);
    Authentication authentication = authenticationManager.authenticate(authenticationToken);
    if (authentication.isAuthenticated()) {
      AuthUser user = (AuthUser) authentication.getPrincipal();
      String newToken = jwtUtil.generateToken(user, 5); // 5min
      response.setHeader("Authorization", "Bearer " + newToken);
    }
  }

  private String extractJwtFromRequest(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    String refreshToken = null;
    for (Cookie cookie : cookies) {
      if ("refreshToken".equals(cookie.getName())) {
        refreshToken = cookie.getValue();
      }
    }
    return refreshToken;
  }
}
