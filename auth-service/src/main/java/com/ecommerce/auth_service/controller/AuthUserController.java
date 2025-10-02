package com.ecommerce.auth_service.controller;

import com.ecommerce.auth_service.dto.UserDto;
import com.ecommerce.auth_service.service.AuthUserRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthUserController {

  private final AuthUserRegistrationService authUserRegistrationService;
  private final PasswordEncoder passwordEncoder;

  AuthUserController(
      AuthUserRegistrationService authUserRegistrationService, PasswordEncoder passwordEncoder) {
    this.authUserRegistrationService = authUserRegistrationService;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/register")
  public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
    userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
    authUserRegistrationService.registerUser(userDto);
    return ResponseEntity.ok("User is Registered!!");
  }
}
