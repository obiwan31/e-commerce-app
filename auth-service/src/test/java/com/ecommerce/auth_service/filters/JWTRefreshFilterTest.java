package com.ecommerce.auth_service.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.auth_service.entity.AuthUser;
import com.ecommerce.auth_service.utils.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class JWTRefreshFilterTest {

  private AuthenticationManager authenticationManager;
  private RedisTemplate<String, Object> redisTemplate;
  private ValueOperations<String, Object> valueOperations;
  private JWTUtil jwtUtil;
  private AuthUser user;
  private Map<String, Object> redisData;

  @BeforeEach
  void setUp() {
    authenticationManager = mock(AuthenticationManager.class);
    redisTemplate = mock(RedisTemplate.class);
    valueOperations = mock(ValueOperations.class);
    jwtUtil = new JWTUtil("01234567890123456789012345678901");

    user = new AuthUser("alice", "alice@example.com", "encoded-password", "ROLE_USER");
    setUserId(user, 1L);
    redisData = new HashMap<>();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(anyString())).thenAnswer(invocation -> redisData.get(invocation.getArgument(0)));
    doAnswer(
            invocation -> {
              redisData.put(invocation.getArgument(0), invocation.getArgument(1));
              return null;
            })
        .when(valueOperations)
        .set(anyString(), any(), any(Duration.class));
    when(authenticationManager.authenticate(any()))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
  }

  @Test
  void refreshTokenRotatesAndReplayOfOldTokenIsRejected() throws Exception {
    String oldRefreshToken = jwtUtil.generateRefreshToken(user, 60);
    redisData.put("refreshToken:" + user.getId(), oldRefreshToken);

    JWTRefreshFilter filter =
        new JWTRefreshFilter(authenticationManager, jwtUtil, redisTemplate, 5, 60, false);

    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/refresh-token");
    request.setCookies(new Cookie("refreshToken", oldRefreshToken));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    String newAccessToken = response.getHeader("Authorization");
    assertNotNull(newAccessToken);
    assertTrue(newAccessToken.startsWith("Bearer "));

    Cookie rotatedCookie = response.getCookie("refreshToken");
    assertNotNull(rotatedCookie);
    assertNotEquals(oldRefreshToken, rotatedCookie.getValue());
    assertEquals("/auth/refresh-token", rotatedCookie.getPath());

    assertEquals(rotatedCookie.getValue(), redisData.get("refreshToken:" + user.getId()));
    verify(valueOperations).set(anyString(), any(), any(Duration.class));

    MockHttpServletRequest replayRequest = new MockHttpServletRequest("POST", "/auth/refresh-token");
    replayRequest.setCookies(new Cookie("refreshToken", oldRefreshToken));
    MockHttpServletResponse replayResponse = new MockHttpServletResponse();
    filter.doFilter(replayRequest, replayResponse, chain);
    assertEquals(401, replayResponse.getStatus());
  }

  @Test
  void refreshEndpointRejectsAccessToken() throws Exception {
    String accessToken = jwtUtil.generateAccessToken(user, 5);
    JWTRefreshFilter filter =
        new JWTRefreshFilter(authenticationManager, jwtUtil, redisTemplate, 5, 60, false);

    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/refresh-token");
    request.setCookies(new Cookie("refreshToken", accessToken));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, mock(FilterChain.class));

    assertEquals(401, response.getStatus());
  }

  private void setUserId(AuthUser authUser, Long id) {
    try {
      Field idField = AuthUser.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(authUser, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
