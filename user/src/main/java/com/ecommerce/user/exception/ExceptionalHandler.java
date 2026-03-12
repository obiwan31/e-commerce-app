package com.ecommerce.user.exception;

import com.ecommerce.user.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class ExceptionalHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage(), "NOT_FOUND");
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
    ErrorResponse error = new ErrorResponse(ex.getMessage(), "BAD_REQUEST");
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .orElse("Validation failed");
    ErrorResponse error = new ErrorResponse(message, "BAD_REQUEST");
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handle(Exception ex) {
    String message = ex.getMessage();
    String errorMessage = "Unexpected server error";
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    if (message != null && message.contains("Duplicate entry")) {
      status = HttpStatus.CONFLICT;
      if (message.contains("uk_user_email")) {
        errorMessage = "Email already registered!";
      } else if (message.contains("uk_user_username")) {
        errorMessage = "Username already registered!";
      } else if (message.contains("uk_user_mobileNumber")) {
        errorMessage = "Mobile Number already registered!";
      }
    }

    ErrorResponse error = new ErrorResponse(errorMessage, status.name());
    return new ResponseEntity<>(error, status);
  }
}
