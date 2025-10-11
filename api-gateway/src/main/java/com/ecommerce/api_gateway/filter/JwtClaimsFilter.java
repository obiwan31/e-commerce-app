package com.ecommerce.api_gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtClaimsFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .flatMap(
            auth -> {
              if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();

                String username = jwt.getSubject();
                String role = jwt.getClaim("role");
                String email = jwt.getClaim("email");
                Long userId = jwt.getClaim("userId");

                ServerHttpRequest mutatedRequest =
                    exchange
                        .getRequest()
                        .mutate()
                        .header("x-user-name", username)
                        .header("x-user-email", email)
                        .header("x-user-id", String.valueOf(userId))
                        .header("x-role", role)
                        .build();

                ServerWebExchange mutatedExchange =
                    exchange.mutate().request(mutatedRequest).build();
                return chain.filter(mutatedExchange);
              }

              return chain.filter(exchange);
            })
        // If no authentication present, continue without modification
        .switchIfEmpty(chain.filter(exchange));
  }

  @Override
  public int getOrder() {
    return 0; // Runs after security context is set
  }
}
