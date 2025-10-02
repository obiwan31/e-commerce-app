package com.ecommerce.user.client;

import com.ecommerce.user.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("auth-service")
public interface AuthUserClient {

  @PostMapping("/auth/register")
  ResponseEntity<String> registerUser(@RequestBody UserDto userDto);
}
