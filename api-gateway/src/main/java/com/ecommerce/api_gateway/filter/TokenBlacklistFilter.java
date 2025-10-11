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
public class TokenBlacklistFilter implements WebFilter {

  private final RedisService redisService;

  public TokenBlacklistFilter(RedisService redisService) {
    this.redisService = redisService;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String requestPath = exchange.getRequest().getPath().value();
    if (!"/auth/refresh-token".equals(requestPath)) {
      return chain.filter(exchange);
    }

    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .cast(JwtAuthenticationToken.class)
        .flatMap(
            auth -> {
              Jwt jwt = auth.getToken();
              Long userId = jwt.getClaim("userId");

              return redisService
                  .isTokenAvailable(userId)
                  .flatMap(
                      isTokenAvailable -> {
                        if (Boolean.FALSE.equals(isTokenAvailable)) {
                          return Mono.error(new AccessDeniedException("Token is blacklisted"));
                        }
                        return chain.filter(exchange);
                      });
            })
        .switchIfEmpty(chain.filter(exchange))
        .onErrorResume(Mono::error);
  }
}
