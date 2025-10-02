package com.ecommerce.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

  private static final String SECRET_KEY = "m9LpQw7xJZ2tVk6eTwYfA3RcNsHdXp1K";
  private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

  public static Claims validateAndExtractClaims(String jwtToken) {
    try {
      return Jwts.parser().setSigningKey(key).build().parseClaimsJws(jwtToken).getBody();
    } catch (JwtException e) {
      // You could log e.getMessage() here for debugging
      return null;
    }
  }
}
