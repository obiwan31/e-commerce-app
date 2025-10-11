package com.ecommerce.api_gateway.filter;

import com.ecommerce.api_gateway.service.RedisService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class UserDeletionFilter implements WebFilter {

  private final RedisService redisService;

  public UserDeletionFilter(RedisService redisService) {
    this.redisService = redisService;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .cast(JwtAuthenticationToken.class)
        .flatMap(
            auth -> {
              Jwt jwt = auth.getToken();
              Long userId = jwt.getClaim("userId");

              return redisService
                  .isUserDeleted(userId)
                  .flatMap(
                      isDeleted -> {
                        if (Boolean.TRUE.equals(isDeleted)) {
                          return Mono.error(new AccessDeniedException("User is deleted"));
                        }
                        return chain.filter(exchange);
                      });
            })
        .switchIfEmpty(chain.filter(exchange))
        .onErrorResume(e -> Mono.error(e));
  }
}
