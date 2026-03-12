package com.ecommerce.auth_service.filters;

import com.ecommerce.auth_service.dto.LoginRequest;
import com.ecommerce.auth_service.entity.AuthUser;
import com.ecommerce.auth_service.utils.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

public class JWTAuthenticationFilter extends OncePerRequestFilter {
  private static final String AUTHORIZATION = "Authorization";
  private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

  private final AuthenticationManager authenticationManager;
  private final JWTUtil jwtUtil;
  private final RedisTemplate<String, Object> redisTemplate;
  private final long accessTokenMinutes;
  private final long refreshTokenMinutes;
  private final boolean refreshCookieSecure;

  public JWTAuthenticationFilter(
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

    if (!request.getServletPath().equals("/auth/generate-token")) {
      filterChain.doFilter(request, response);
      return;
    }

    ObjectMapper objectMapper = new ObjectMapper();
    LoginRequest loginRequest =
        objectMapper.readValue(request.getInputStream(), LoginRequest.class);
    String principal = resolvePrincipal(loginRequest);
    if (principal == null || principal.isBlank() || loginRequest.getPassword() == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Username/email and password are required\"}");
      return;
    }

    String loginAttemptsKey = "loginAttempts:ip:" + resolveClientIp(request);
    if (isBlocked(loginAttemptsKey)) {
      response.setStatus(429);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Too many failed login attempts. Try again later.\"}");
      return;
    }

    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
        new UsernamePasswordAuthenticationToken(principal, loginRequest.getPassword());
    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
    } catch (AuthenticationException ex) {
      registerFailedAttempt(loginAttemptsKey);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Invalid username/email or password\"}");
      return;
    }

    if (authentication.isAuthenticated()) {
      AuthUser user = (AuthUser) authentication.getPrincipal();
      String token = jwtUtil.generateAccessToken(user, accessTokenMinutes);
      response.setHeader(AUTHORIZATION, "Bearer " + token);

      String refreshToken = jwtUtil.generateRefreshToken(user, refreshTokenMinutes);
      // Set Refresh Token in HttpOnly Cookie
      Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
      refreshCookie.setHttpOnly(true); // prevent javascript from accessing it
      refreshCookie.setSecure(refreshCookieSecure); // sent only over HTTPS
      refreshCookie.setPath("/auth/refresh-token"); // Cookie available only for refresh endpoint
      refreshCookie.setMaxAge((int) Duration.ofMinutes(refreshTokenMinutes).getSeconds());
      response.addCookie(refreshCookie);

      String refreshTokenKey = "refreshToken:" + user.getId();
      redisTemplate
          .opsForValue()
          .set(refreshTokenKey, refreshToken, Duration.ofMinutes(refreshTokenMinutes));
      redisTemplate.delete(loginAttemptsKey);
    }
  }

  private String resolvePrincipal(LoginRequest loginRequest) {
    if (loginRequest.getUsername() != null && !loginRequest.getUsername().isBlank()) {
      return loginRequest.getUsername();
    }
    return loginRequest.getEmail();
  }

  private boolean isBlocked(String key) {
    Object value = redisTemplate.opsForValue().get(key);
    if (value == null) {
      return false;
    }
    long attempts = Long.parseLong(value.toString());
    return attempts >= 5;
  }

  private void registerFailedAttempt(String key) {
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1L) {
      redisTemplate.expire(key, Duration.ofMinutes(5));
    }
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return realIp.trim();
    }
    return request.getRemoteAddr();
  }
}
