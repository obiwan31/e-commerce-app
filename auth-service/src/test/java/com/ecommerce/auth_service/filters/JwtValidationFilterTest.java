package com.ecommerce.auth_service.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.ecommerce.auth_service.entity.AuthUser;
import com.ecommerce.auth_service.utils.JWTUtil;
import jakarta.servlet.FilterChain;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;

class JwtValidationFilterTest {

  @Test
  void rejectsRefreshTokenOnProtectedRequests() throws Exception {
    JWTUtil jwtUtil = new JWTUtil("01234567890123456789012345678901");
    AuthUser user = new AuthUser("bob", "bob@example.com", "encoded-password", "ROLE_USER");
    setUserId(user, 7L);
    String refreshToken = jwtUtil.generateRefreshToken(user, 60);

    AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    JwtValidationFilter filter = new JwtValidationFilter(authenticationManager, jwtUtil);

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/me");
    request.addHeader("Authorization", "Bearer " + refreshToken);
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
    verifyNoInteractions(authenticationManager);
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
