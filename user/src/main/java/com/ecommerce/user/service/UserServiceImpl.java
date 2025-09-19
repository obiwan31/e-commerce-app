package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public ResponseEntity<UserResponse> registerUser(UserRequest userRequest) {
    User user =
        User.builder()
            .age(userRequest.getAge())
            .name(userRequest.getName())
            .mobileNumber(userRequest.getMobileNumber())
            .email(userRequest.getEmail())
            .build();
    userRepository.save(user);

    UserResponse userResponse = new UserResponse();
    userResponse.setMessage("Hello " + user.getName() + ", Your registration is successful");
    return ResponseEntity.ok(userResponse);
  }
}
