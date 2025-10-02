package com.ecommerce.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto {

  private String name;
  private String username;
  private String password;
  private int age;
  private String mobileNumber;
  private String email;
  private String role;
}
