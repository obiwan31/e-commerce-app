package com.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDto {
  @NotNull(message = "User ID is required")
  @Min(value = 1, message = "User ID must be greater than 0")
  private Long userId;

  @NotNull(message = "Address ID is required")
  @Min(value = 1, message = "Address ID must be greater than 0")
  private Long addressId;

  @NotBlank(message = "Payment method is required")
  private String paymentMethod;
}
