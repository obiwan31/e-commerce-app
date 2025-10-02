package com.ecommerce.auth_service.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
  private final String token;

  public JwtAuthenticationToken(String token) {
    super(null);
    this.token = token;
    setAuthenticated(false);
  }

  public String getToken() {
    return token;
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return null;
  }
}
