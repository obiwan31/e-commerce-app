package com.ecommerce.api_gateway.config;

import java.util.Optional;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {
  @Bean
  public KeyResolver userOrIpKeyResolver() {
    return exchange ->
        ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .filter(auth -> auth instanceof JwtAuthenticationToken)
            .cast(JwtAuthenticationToken.class)
            .map(JwtAuthenticationToken::getToken)
            .map(this::extractUserId)
            .switchIfEmpty(
                exchange
                    .getRequest()
                    .getHeaders()
                    .getOrEmpty("X-Forwarded-For")
                    .stream()
                    .findFirst()
                    .map(value -> value.split(",")[0].trim())
                    .map(Mono::just)
                    .orElseGet(
                        () ->
                            Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                                .map(addr -> addr.getAddress().getHostAddress())
                                .map(Mono::just)
                                .orElseGet(() -> Mono.just("anonymous"))));
  }

  private String extractUserId(Jwt jwt) {
    Object userId = jwt.getClaims().get("userId");
    if (userId == null) {
      return "anonymous";
    }
    return String.valueOf(userId);
  }
}
