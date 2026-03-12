package com.ecommerce.user.controller;

import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.exception.BadRequestException;
import com.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User profile management endpoints")
public class UserController {
  private static final String USER_ID = "x-user-id";

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  @Operation(summary = "Register a new user")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "User registered successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input")
  })
  public ResponseEntity<String> registerUser(@Valid @RequestBody UserDto userDto) {
    Long userId = userService.registerUser(userDto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body("Hello " + userDto.getName() + ":" + userId + ", Your registration is successful");
  }

  @GetMapping
  @Operation(summary = "Get current user by x-user-id header")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User retrieved"),
    @ApiResponse(responseCode = "400", description = "Missing or invalid x-user-id header")
  })
  public ResponseEntity<UserDto> getUser(@RequestHeader Map<String, String> headers) {
    Long userId = extractUserId(headers);
    return ResponseEntity.ok(userService.getUser(userId));
  }

  @GetMapping("/all")
  @Operation(summary = "Get all users")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Users retrieved")})
  public ResponseEntity<List<UserDto>> getUsers() {
    return ResponseEntity.ok(userService.getUsers());
  }

  @DeleteMapping
  @Operation(summary = "Delete current user by x-user-id header")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User deleted"),
    @ApiResponse(responseCode = "400", description = "Missing or invalid x-user-id header")
  })
  public ResponseEntity<String> deleteUser(@RequestHeader Map<String, String> headers) {
    Long userId = extractUserId(headers);
    userService.deleteUser(userId);
    return ResponseEntity.ok("User deleted successfully with ID: " + userId);
  }

  private Long extractUserId(Map<String, String> headers) {
    String userIdHeader = headers.get(USER_ID);
    if (userIdHeader == null || userIdHeader.isBlank()) {
      throw new BadRequestException("Missing required header: x-user-id");
    }
    try {
      return Long.valueOf(userIdHeader);
    } catch (NumberFormatException ex) {
      throw new BadRequestException("Invalid x-user-id header value");
    }
  }
}
