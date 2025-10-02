package com.ecommerce.auth_service.filters;

import com.ecommerce.auth_service.token.JwtAuthenticationToken;
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

  public JwtValidationFilter(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
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
      JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(token);
      Authentication authentication = authenticationManager.authenticate(jwtAuthenticationToken);
      if (authentication.isAuthenticated()) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    filterChain.doFilter(request, response);
  }
}
