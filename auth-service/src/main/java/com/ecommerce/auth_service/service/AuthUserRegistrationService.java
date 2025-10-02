package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.dto.UserDto;
import com.ecommerce.auth_service.entity.AuthUser;
import com.ecommerce.auth_service.repository.AuthUserRegistrationRepository;
import com.ecommerce.auth_service.utils.AuthUtil;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserRegistrationService implements UserDetailsService {

  private final AuthUserRegistrationRepository authUserRegistrationRepository;

  public AuthUserRegistrationService(
      AuthUserRegistrationRepository authUserRegistrationRepository) {
    this.authUserRegistrationRepository = authUserRegistrationRepository;
  }

  public void registerUser(UserDto userDto) {
    AuthUser authUser =
        new AuthUser(
            userDto.getUsername(), userDto.getEmail(), userDto.getPassword(), userDto.getRole());
    authUserRegistrationRepository.save(authUser);
  }

  @Override
  public AuthUser loadUserByUsername(String principal) throws UsernameNotFoundException {
    if (AuthUtil.isValidEmail(principal)) {
      return authUserRegistrationRepository
          .findByEmail(principal)
          .orElseThrow(() -> new UsernameNotFoundException("User not found!!"));
    }
    return authUserRegistrationRepository
        .findByUsername(principal)
        .orElseThrow(() -> new UsernameNotFoundException("User not found!!"));
  }
}
