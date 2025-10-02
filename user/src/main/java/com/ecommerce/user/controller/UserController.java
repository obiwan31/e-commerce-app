package com.ecommerce.user.controller;

import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponse> registerUser(@RequestBody UserDto userDto) {
    return userService.registerUser(userDto);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> getUser(@PathVariable("userId") Long userId) {
    return userService.getUser(userId);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<String> deleteUser(@PathVariable("userId") Long userId) {
    return userService.deleteUser(userId);
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> getUsers() {
    return userService.getUsers();
  }
}
