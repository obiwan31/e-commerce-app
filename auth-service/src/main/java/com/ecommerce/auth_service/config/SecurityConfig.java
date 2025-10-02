package com.ecommerce.auth_service.config;

import com.ecommerce.auth_service.authenticationProvider.JWTAuthenticationProvider;
import com.ecommerce.auth_service.filters.JWTAuthenticationFilter;
import com.ecommerce.auth_service.filters.JWTRefreshFilter;
import com.ecommerce.auth_service.filters.JwtValidationFilter;
import com.ecommerce.auth_service.utils.JWTUtil;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

  public SecurityConfig(JWTUtil jwtUtil, UserDetailsService userDetailsService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
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
        new JWTAuthenticationFilter(authenticationManager, jwtUtil);

    // Validation filter for checking JWT in every request
    JwtValidationFilter jwtValidationFilter = new JwtValidationFilter(authenticationManager);

    // refresh filter for checking JWT in every request
    JWTRefreshFilter jwtRefreshFilter = new JWTRefreshFilter(authenticationManager, jwtUtil);

    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/register")
                    .permitAll()
                    .requestMatchers("/auth/users")
                    .hasRole("USER") // Role
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
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
