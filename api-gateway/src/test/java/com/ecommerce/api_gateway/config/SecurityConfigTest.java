package com.ecommerce.api_gateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

class SecurityConfigTest {

  @Test
  void converterNormalizesRolePrefix() {
    SecurityConfig config = new SecurityConfig("01234567890123456789012345678901");
    Jwt jwt =
        Jwt.withTokenValue("t1")
            .header("alg", "HS256")
            .claim("role", "ROLE_ADMIN")
            .claim("userId", 1L)
            .build();

    AbstractAuthenticationToken token = config.jwtAuthenticationConverter().convert(jwt).block();

    assertTrue(token != null);
    String authorities =
        token.getAuthorities().stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
    assertEquals("ROLE_ADMIN", authorities);
  }

  @Test
  void converterDefaultsMissingRoleToUser() {
    SecurityConfig config = new SecurityConfig("01234567890123456789012345678901");
    Jwt jwt = Jwt.withTokenValue("t2").header("alg", "HS256").claim("userId", 2L).build();

    AbstractAuthenticationToken token = config.jwtAuthenticationConverter().convert(jwt).block();

    assertTrue(token != null);
    String authorities =
        token.getAuthorities().stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));
    assertEquals("ROLE_USER", authorities);
  }
}
