package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;

public interface UserService {

  ResponseEntity<UserResponse> registerUser(UserRequest userRequest);
}
