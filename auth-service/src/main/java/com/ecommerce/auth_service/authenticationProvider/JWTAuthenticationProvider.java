package com.ecommerce.auth_service.authenticationProvider;

import com.ecommerce.auth_service.token.JwtAuthenticationToken;
import com.ecommerce.auth_service.utils.JWTUtil;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class JWTAuthenticationProvider implements AuthenticationProvider {

  private final JWTUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  public JWTAuthenticationProvider(JWTUtil jwtUtil, UserDetailsService userDetailsService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String token = ((JwtAuthenticationToken) authentication).getToken();

    String username = jwtUtil.validateAndExtractUsername(token);
    if (username == null) {
      throw new BadCredentialsException("Invalid JWT Token");
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return JwtAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
