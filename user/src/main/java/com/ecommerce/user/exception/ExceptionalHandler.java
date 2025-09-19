package com.ecommerce.user.exception;

import com.ecommerce.user.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionalHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handle(Exception ex) {
    String message = ex.getMessage();
    String errorMessage = "";

    if (message != null && message.contains("Duplicate entry")) {
      if (message.contains("uk_user_email")) {
        errorMessage = "Email already registered!";
      } else if (message.contains("uk_user_mobileNumber")) {
        errorMessage = "Mobile Number already registered!";
      }
    }

    ErrorResponse error = new ErrorResponse(errorMessage, "BAD_REQUEST");
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
}
