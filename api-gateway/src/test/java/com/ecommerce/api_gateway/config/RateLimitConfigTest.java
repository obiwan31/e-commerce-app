package com.ecommerce.api_gateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.test.StepVerifier;

class RateLimitConfigTest {

  @Test
  void keyResolverUsesUserIdWhenJwtIsPresent() {
    RateLimitConfig config = new RateLimitConfig();
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders").build());

    Jwt jwt = Jwt.withTokenValue("t").header("alg", "HS256").claim("userId", 42L).build();
    JwtAuthenticationToken authentication =
        new JwtAuthenticationToken(jwt, java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));

    StepVerifier.create(
            config
                .userOrIpKeyResolver()
                .resolve(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
        .expectNext("42")
        .verifyComplete();
  }

  @Test
  void keyResolverFallsBackToRemoteIpWhenNoJwt() {
    RateLimitConfig config = new RateLimitConfig();
    MockServerHttpRequest request =
        MockServerHttpRequest.get("/products")
            .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    StepVerifier.create(config.userOrIpKeyResolver().resolve(exchange))
        .assertNext(value -> assertEquals("127.0.0.1", value))
        .verifyComplete();
  }
}
