package com.ecommerce.user.service;

import com.ecommerce.user.client.AuthUserClient;
import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
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
  public Long registerUser(UserDto userDto) {
    User user =
        User.builder()
            .age(userDto.getAge())
            .name(userDto.getName())
            .username(userDto.getUsername())
            .mobileNumber(userDto.getMobileNumber())
            .email(userDto.getEmail())
            .build();

    User savedUser = userRepository.save(user);

    // Call Auth-User API to register for JWT login
    authUserClient.registerUser(userDto);

    return savedUser.getId();
  }

  @Override
  public UserDto getUser(Long userId) {
    Optional<User> user = userRepository.findById(userId);
    return user.map(value -> this.getUserDto(value)).orElse(null);
  }

  @Override
  public List<UserDto> getUsers() {
    return userRepository.findAll().stream().map(this::getUserDto).toList();
  }

  @Override
  public void deleteUser(Long userId) {
    userRepository.deleteById(userId);
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
