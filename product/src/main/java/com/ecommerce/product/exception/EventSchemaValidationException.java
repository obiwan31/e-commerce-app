package com.ecommerce.product.exception;

public class EventSchemaValidationException extends BadRequestException {
  public EventSchemaValidationException(String message) {
    super(message);
  }
}
