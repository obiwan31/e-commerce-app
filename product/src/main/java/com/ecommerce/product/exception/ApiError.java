package com.ecommerce.product.exception;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApiError {
  private Instant timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
}
