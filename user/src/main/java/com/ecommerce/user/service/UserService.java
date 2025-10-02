package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.dto.UserResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface UserService {

  ResponseEntity<UserResponse> registerUser(UserDto userDto);

  ResponseEntity<UserDto> getUser(Long userId);

  ResponseEntity<List<UserDto>> getUsers();

  ResponseEntity<String> deleteUser(Long userId);
}
