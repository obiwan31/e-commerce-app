package com.ecommerce.auth_service.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuthRateLimitFilter extends OncePerRequestFilter {

  private static final int LOGIN_MAX_REQUESTS = 5;
  private static final int REGISTER_MAX_REQUESTS = 10;
  private static final Duration WINDOW = Duration.ofMinutes(1);

  private final ConcurrentMap<String, Deque<Instant>> requestsByKey = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = request.getServletPath();
    int limit = resolveLimit(path);
    if (limit == -1) {
      filterChain.doFilter(request, response);
      return;
    }

    String key = path + ":" + resolveClientIp(request);
    if (isRateLimitExceeded(key, limit)) {
      response.setStatus(429);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Too many requests, please try again later\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private int resolveLimit(String path) {
    if ("/auth/generate-token".equals(path)) {
      return LOGIN_MAX_REQUESTS;
    }
    if ("/auth/register".equals(path)) {
      return REGISTER_MAX_REQUESTS;
    }
    return -1;
  }

  private boolean isRateLimitExceeded(String key, int limit) {
    Instant now = Instant.now();
    Deque<Instant> requests = requestsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());
    synchronized (requests) {
      while (!requests.isEmpty() && Duration.between(requests.peekFirst(), now).compareTo(WINDOW) > 0) {
        requests.pollFirst();
      }
      if (requests.size() >= limit) {
        return true;
      }
      requests.addLast(now);
      return false;
    }
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return realIp.trim();
    }
    return request.getRemoteAddr();
  }
}
