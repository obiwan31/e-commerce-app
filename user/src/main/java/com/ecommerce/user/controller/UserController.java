package com.ecommerce.user.controller;

import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.service.UserService;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
  private static final String USER_ID = "x-user-id";

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
    Long userId = userService.registerUser(userDto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body("Hello " + userDto.getName() + ":" + userId + ", Your registration is successful");
  }

  @GetMapping
  public ResponseEntity<UserDto> getUser(@RequestHeader Map<String, String> headers) {
    Long userId = Long.valueOf(headers.get(USER_ID));
    UserDto userDto = userService.getUser(userId);
    if (userDto != null) {
      return ResponseEntity.ok(userDto);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/all")
  public ResponseEntity<List<UserDto>> getUsers() {
    return ResponseEntity.ok(userService.getUsers());
  }

  @DeleteMapping
  public ResponseEntity<String> deleteUser(@RequestHeader Map<String, String> headers) {
    Long userId = Long.valueOf(headers.get(USER_ID));
    try {
      userService.deleteUser(userId);
      return ResponseEntity.ok("User deleted successfully with ID: " + userId);
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage());
      return ResponseEntity.notFound().build();
    }
  }
}
