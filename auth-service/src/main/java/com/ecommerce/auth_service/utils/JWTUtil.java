package com.ecommerce.auth_service.utils;

import com.ecommerce.auth_service.entity.AuthUser;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

  private static final String SECRET_KEY = "m9LpQw7xJZ2tVk6eTwYfA3RcNsHdXp1K";
  private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

  public String generateToken(AuthUser authUser, long expireMinutes) {
    return Jwts.builder()
        .subject(authUser.getUsername())
        .claim("email", authUser.getEmail())
        .claim("userId", authUser.getId())
        .claim("role", authUser.getRole())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000))
        .signWith(key)
        .compact();
  }

  public String validateAndExtractUsername(String token) {
    try {
      return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    } catch (JwtException e) {
      return null;
    }
  }
}
