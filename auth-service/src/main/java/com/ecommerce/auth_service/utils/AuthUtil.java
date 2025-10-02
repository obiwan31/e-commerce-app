package com.ecommerce.auth_service.utils;

public class AuthUtil {
  public AuthUtil() {}

  public static boolean isValidEmail(String input) {
    return input.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
  }
}
