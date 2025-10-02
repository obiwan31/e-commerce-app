package com.ecommerce.api_gateway.globalFilter;

import com.ecommerce.api_gateway.util.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

    String uriPath = exchange.getRequest().getURI().getPath();

    // Skip JWT check for open endpoints
    if (uriPath.startsWith("/auth")) {
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    String jwtToken = authHeader.substring(7);
    try {
      Claims claims = JWTUtil.validateAndExtractClaims(jwtToken);

      String username = claims.getSubject();
      String role = claims.get("role", String.class);
      String email = claims.get("email", String.class);
      Long userId = claims.get("userId", Long.class);

      // Inject claims into request header
      ServerHttpRequest mutatedRequest =
          exchange
              .getRequest()
              .mutate()
              .header("X-User-Name", username)
              .header("X-User-Email", email)
              .header("X-User-Id", String.valueOf(userId))
              .header("X-Role", role)
              .build();

      ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

      return chain.filter(mutatedExchange);

    } catch (Exception e) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }
  }

  @Override
  public int getOrder() {
    return -1; // highest priority
  }
}
