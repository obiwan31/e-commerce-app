package com.ecommerce.user.service;

import com.ecommerce.user.client.AuthUserClient;
import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private final AuthUserClient authUserClient;
  private final UserRepository userRepository;

  public UserServiceImpl(AuthUserClient authUserClient, UserRepository userRepository) {
    this.authUserClient = authUserClient;
    this.userRepository = userRepository;
  }

  @Override
  public ResponseEntity<UserResponse> registerUser(UserDto userDto) {
    User user =
        User.builder()
            .age(userDto.getAge())
            .name(userDto.getName())
            .username(userDto.getUsername())
            .mobileNumber(userDto.getMobileNumber())
            .email(userDto.getEmail())
            .build();
    userRepository.save(user);

    // Call Auth-User API to register into JWT login
    authUserClient.registerUser(userDto);

    UserResponse userResponse = new UserResponse();
    userResponse.setMessage("Hello " + user.getName() + ", Your registration is successful");
    return ResponseEntity.ok(userResponse);
  }

  @Override
  public ResponseEntity<UserDto> getUser(Long userId) {
    Optional<User> user = userRepository.findById(userId);
    return user.map(value -> ResponseEntity.ok(this.getUserDto(value)))
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
  }

  @Override
  public ResponseEntity<List<UserDto>> getUsers() {
    List<UserDto> users = userRepository.findAll().stream().map(this::getUserDto).toList();
    return ResponseEntity.ok(users);
  }

  @Override
  public ResponseEntity<String> deleteUser(Long userId) {
    userRepository.deleteById(userId);
    return ResponseEntity.ok("User Deleted!");
  }

  private UserDto getUserDto(User user) {
    return UserDto.builder()
        .name(user.getName())
        .username(user.getUsername())
        .email(user.getEmail())
        .mobileNumber(user.getMobileNumber())
        .age(user.getAge())
        .build();
  }
}
