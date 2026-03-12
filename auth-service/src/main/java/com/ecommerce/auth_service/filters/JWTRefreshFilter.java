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
import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

public class JWTRefreshFilter extends OncePerRequestFilter {
  private static final String AUTHORIZATION = "Authorization";
  private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

  private final AuthenticationManager authenticationManager;
  private final JWTUtil jwtUtil;
  private final RedisTemplate<String, Object> redisTemplate;
  private final long accessTokenMinutes;
  private final long refreshTokenMinutes;
  private final boolean refreshCookieSecure;

  public JWTRefreshFilter(
      AuthenticationManager authenticationManager,
      JWTUtil jwtUtil,
      RedisTemplate<String, Object> redisTemplate,
      long accessTokenMinutes,
      long refreshTokenMinutes,
      boolean refreshCookieSecure) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.redisTemplate = redisTemplate;
    this.accessTokenMinutes = accessTokenMinutes;
    this.refreshTokenMinutes = refreshTokenMinutes;
    this.refreshCookieSecure = refreshCookieSecure;
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
    if (refreshToken == null || !jwtUtil.isTokenType(refreshToken, JWTUtil.TOKEN_TYPE_REFRESH)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    Long userId = jwtUtil.validateAndExtractUserId(refreshToken);
    if (userId == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    Object cachedRefreshToken = redisTemplate.opsForValue().get("refreshToken:" + userId);
    if (!(cachedRefreshToken instanceof String cachedToken)
        || !refreshToken.equals(cachedToken)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(refreshToken);
    Authentication authentication = authenticationManager.authenticate(authenticationToken);
    if (authentication.isAuthenticated()) {
      AuthUser user = (AuthUser) authentication.getPrincipal();
      String newAccessToken = jwtUtil.generateAccessToken(user, accessTokenMinutes);
      String rotatedRefreshToken = jwtUtil.generateRefreshToken(user, refreshTokenMinutes);

      redisTemplate
          .opsForValue()
          .set(
              "refreshToken:" + user.getId(),
              rotatedRefreshToken,
              Duration.ofMinutes(refreshTokenMinutes));

      Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, rotatedRefreshToken);
      refreshCookie.setHttpOnly(true);
      refreshCookie.setSecure(refreshCookieSecure);
      refreshCookie.setPath("/auth/refresh-token");
      refreshCookie.setMaxAge((int) Duration.ofMinutes(refreshTokenMinutes).getSeconds());
      response.addCookie(refreshCookie);

      response.setHeader(AUTHORIZATION, "Bearer " + newAccessToken);
    }
  }

  private String extractJwtFromRequest(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    String refreshToken = null;
    for (Cookie cookie : cookies) {
      if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
        refreshToken = cookie.getValue();
      }
    }
    return refreshToken;
  }
}
