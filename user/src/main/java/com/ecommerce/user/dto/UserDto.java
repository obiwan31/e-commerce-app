package com.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDto {

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must be at most 100 characters")
  private String name;

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password;

  @Min(value = 13, message = "Age must be at least 13")
  @Max(value = 120, message = "Age must be at most 120")
  private int age;

  @NotBlank(message = "Mobile number is required")
  @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile number must be 10 to 15 digits")
  private String mobileNumber;

  @NotBlank(message = "Email is required")
  @Email(message = "Email format is invalid")
  private String email;

  @NotBlank(message = "Role is required")
  private String role;
}
