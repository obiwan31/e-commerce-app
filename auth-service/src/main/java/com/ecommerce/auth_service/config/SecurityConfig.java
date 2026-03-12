package com.ecommerce.auth_service.config;

import com.ecommerce.auth_service.authenticationProvider.JWTAuthenticationProvider;
import com.ecommerce.auth_service.filters.AuthRateLimitFilter;
import com.ecommerce.auth_service.filters.JWTAuthenticationFilter;
import com.ecommerce.auth_service.filters.JWTRefreshFilter;
import com.ecommerce.auth_service.filters.JwtValidationFilter;
import com.ecommerce.auth_service.utils.JWTUtil;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JWTUtil jwtUtil;
  private final UserDetailsService userDetailsService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final long accessTokenMinutes;
  private final long refreshTokenMinutes;
  private final boolean refreshCookieSecure;

  public SecurityConfig(
      JWTUtil jwtUtil,
      UserDetailsService userDetailsService,
      RedisTemplate<String, Object> redisTemplate,
      @Value("${security.jwt.access-token-minutes:5}") long accessTokenMinutes,
      @Value("${security.jwt.refresh-token-minutes:10080}") long refreshTokenMinutes,
      @Value("${security.jwt.refresh-cookie-secure:true}") boolean refreshCookieSecure) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
    this.redisTemplate = redisTemplate;
    this.accessTokenMinutes = accessTokenMinutes;
    this.refreshTokenMinutes = refreshTokenMinutes;
    this.refreshCookieSecure = refreshCookieSecure;
  }

  @Bean
  public JWTAuthenticationProvider jwtAuthenticationProvider() {
    return new JWTAuthenticationProvider(jwtUtil, userDetailsService);
  }

  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, AuthenticationManager authenticationManager, JWTUtil jwtUtil)
      throws Exception {

    // Authentication filter responsible for login
    JWTAuthenticationFilter jwtAuthFilter =
        new JWTAuthenticationFilter(
            authenticationManager,
            jwtUtil,
            redisTemplate,
            accessTokenMinutes,
            refreshTokenMinutes,
            refreshCookieSecure);

    // Validation filter for checking JWT in every request
    JwtValidationFilter jwtValidationFilter = new JwtValidationFilter(authenticationManager, jwtUtil);

    // refresh filter for checking JWT in every request
    JWTRefreshFilter jwtRefreshFilter =
        new JWTRefreshFilter(
            authenticationManager,
            jwtUtil,
            redisTemplate,
            accessTokenMinutes,
            refreshTokenMinutes,
            refreshCookieSecure);
    AuthRateLimitFilter authRateLimitFilter = new AuthRateLimitFilter();

    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/auth/register",
                        "/auth/users",
                        "/auth/generate-token",
                        "/auth/refresh-token",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
        .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(
            jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // generate token filter
        .addFilterAfter(jwtValidationFilter, JWTAuthenticationFilter.class) // validate token filter
        .addFilterAfter(jwtRefreshFilter, JwtValidationFilter.class); // refresh token filter
    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager() {
    return new ProviderManager(
        Arrays.asList(daoAuthenticationProvider(), jwtAuthenticationProvider()));
  }
}
