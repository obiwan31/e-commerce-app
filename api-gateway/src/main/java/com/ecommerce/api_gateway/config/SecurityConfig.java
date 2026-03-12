package com.ecommerce.api_gateway.config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

  private final String jwtSecret;

  public SecurityConfig(@Value("${security.jwt.secret}") String jwtSecret) {
    this.jwtSecret = jwtSecret;
  }

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http,
      ReactiveJwtDecoder jwtDecoder,
      Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {

    http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers(
                        "/auth/**",
                        "/users/register",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    .pathMatchers(HttpMethod.GET, "/users/all")
                    .hasRole("ADMIN")
                    .pathMatchers(HttpMethod.POST, "/products/**")
                    .hasRole("ADMIN")
                    .pathMatchers(HttpMethod.PUT, "/products/**")
                    .hasRole("ADMIN")
                    .pathMatchers(HttpMethod.DELETE, "/products/**")
                    .hasRole("ADMIN")
                    .pathMatchers("/carts/**", "/orders/**")
                    .hasAnyRole("USER", "ADMIN")
                    .pathMatchers(HttpMethod.DELETE, "/users/**")
                    .hasAnyRole("USER", "ADMIN")
                    .pathMatchers(HttpMethod.GET, "/users/**")
                    .hasAnyRole("USER", "ADMIN")
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.jwtDecoder(jwtDecoder)
                            .jwtAuthenticationConverter(jwtAuthenticationConverter)));

    return http.build();
  }

  @Bean
  public ReactiveJwtDecoder jwtDecoder() {
    if (jwtSecret == null || jwtSecret.isBlank()) {
      throw new IllegalStateException("security.jwt.secret must be configured");
    }
    return NimbusReactiveJwtDecoder.withSecretKey(
            new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
        .build();
  }

  @Bean
  public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
    return jwt -> {
      String role = jwt.getClaimAsString("role");
      if (role == null || role.isBlank()) {
        role = "USER";
      }
      if (role != null && role.startsWith("ROLE_")) {
        role = role.substring("ROLE_".length());
      }
      Collection<GrantedAuthority> authorities =
          List.of(new SimpleGrantedAuthority("ROLE_" + role));
      return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    };
  }
}
