package com.ecommerce.api_gateway.filter;

import com.ecommerce.api_gateway.service.RedisService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoginAttemptFilter implements GlobalFilter, Ordered {

  private final RedisService redisService;

  public LoginAttemptFilter(RedisService redisService) {
    this.redisService = redisService;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .flatMap(
            auth -> {
              if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                Long userId = jwt.getClaim("userId");
                return redisService
                    .hasLoginAttemptExceeded(userId)
                    .flatMap(
                        hasExceeded -> {
                          if (Boolean.TRUE.equals(hasExceeded)) {
                            return Mono.error(
                                new ResponseStatusException(
                                    HttpStatus.TOO_MANY_REQUESTS, "Login attempts exceeded"));
                          }
                          return chain.filter(exchange);
                        });
              }
              return chain.filter(exchange);
            })
        // If no authentication present, continue without modification
        .switchIfEmpty(chain.filter(exchange));
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
