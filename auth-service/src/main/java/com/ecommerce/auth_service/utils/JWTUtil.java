package com.ecommerce.auth_service.utils;

import com.ecommerce.auth_service.entity.AuthUser;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {
  public static final String TOKEN_TYPE_ACCESS = "ACCESS";
  public static final String TOKEN_TYPE_REFRESH = "REFRESH";

  private final Key key;

  public JWTUtil(@Value("${security.jwt.secret}") String jwtSecret) {
    if (jwtSecret == null || jwtSecret.isBlank()) {
      throw new IllegalStateException("security.jwt.secret must be configured");
    }
    this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(AuthUser authUser, long expireMinutes) {
    return generateToken(authUser, expireMinutes, TOKEN_TYPE_ACCESS);
  }

  public String generateRefreshToken(AuthUser authUser, long expireMinutes) {
    return generateToken(authUser, expireMinutes, TOKEN_TYPE_REFRESH);
  }

  private String generateToken(AuthUser authUser, long expireMinutes, String tokenType) {
    return Jwts.builder()
        .subject(authUser.getUsername())
        .claim("email", authUser.getEmail())
        .claim("userId", authUser.getId())
        .claim("role", authUser.getRole())
        .claim("tokenType", tokenType)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000))
        .signWith(key)
        .compact();
  }

  public Claims extractClaims(String token) {
    try {
      return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
    } catch (JwtException e) {
      return null;
    }
  }

  public String validateAndExtractUsername(String token) {
    Claims claims = extractClaims(token);
    return claims != null ? claims.getSubject() : null;
  }

  public Long validateAndExtractUserId(String token) {
    Claims claims = extractClaims(token);
    if (claims == null) {
      return null;
    }
    Object userId = claims.get("userId");
    if (userId instanceof Number number) {
      return number.longValue();
    }
    if (userId instanceof String text) {
      try {
        return Long.parseLong(text);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  public boolean isTokenType(String token, String expectedType) {
    Claims claims = extractClaims(token);
    if (claims == null) {
      return false;
    }
    String tokenType = claims.get("tokenType", String.class);
    return expectedType.equals(tokenType);
  }
}
