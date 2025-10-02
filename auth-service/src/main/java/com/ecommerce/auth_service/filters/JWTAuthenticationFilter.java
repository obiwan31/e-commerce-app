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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

public class JWTAuthenticationFilter extends OncePerRequestFilter {

  private final AuthenticationManager authenticationManager;
  private final JWTUtil jwtUtil;

  public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
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

    String principal =
        loginRequest.getUsername() != null ? loginRequest.getUsername() : loginRequest.getEmail();
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
        new UsernamePasswordAuthenticationToken(principal, loginRequest.getPassword());

    Authentication authentication =
        authenticationManager.authenticate(usernamePasswordAuthenticationToken);

    if (authentication.isAuthenticated()) {
      AuthUser user = (AuthUser) authentication.getPrincipal();
      String token = jwtUtil.generateToken(user, 5);
      response.setHeader("Authorization", "Bearer " + token);

      String refreshToken = jwtUtil.generateToken(user, 7 * 24 * 60); // 7day
      // Set Refresh Token in HttpOnly Cookie
      // we can also send it in response body but then client has to store it in local storage or
      // in-memory
      Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
      refreshCookie.setHttpOnly(true); // prevent javascript from accessing it
      refreshCookie.setSecure(true); // sent only over HTTPS
      refreshCookie.setPath("/refresh-token"); // Cookie available only for refresh endpoint
      refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days expiry
      response.addCookie(refreshCookie);
    }
  }
}
