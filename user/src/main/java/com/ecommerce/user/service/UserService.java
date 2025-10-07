package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserDto;
import java.util.List;

public interface UserService {

  Long registerUser(UserDto userDto);

  UserDto getUser(Long userId);

  List<UserDto> getUsers();

  void deleteUser(Long userId);
}
