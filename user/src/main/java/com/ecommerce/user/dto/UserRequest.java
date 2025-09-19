package com.ecommerce.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

  private String name;
  private int age;
  private String mobileNumber;
  private String email;
}
