package com.ecommerce.order.exception;

public class DownstreamServiceException extends RuntimeException {
  public DownstreamServiceException(String message) {
    super(message);
  }
}
