package com.ecommerce.auth_service.controller;

import com.ecommerce.auth_service.dto.UserDto;
import com.ecommerce.auth_service.service.AuthUserRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthUserController {

  private final AuthUserRegistrationService authUserRegistrationService;
  private final PasswordEncoder passwordEncoder;

  AuthUserController(
      AuthUserRegistrationService authUserRegistrationService, PasswordEncoder passwordEncoder) {
    this.authUserRegistrationService = authUserRegistrationService;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/register")
  @Operation(summary = "Register a new user in auth service")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User registered successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request payload"),
    @ApiResponse(responseCode = "500", description = "Unexpected server error")
  })
  public ResponseEntity<String> registerUser(@Valid @RequestBody UserDto userDto) {
    userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
    authUserRegistrationService.registerUser(userDto);
    return ResponseEntity.ok("User is Registered!!");
  }
}
