package com.ecommerce.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequestDto {
  private Long userId;
  private Long addressId;
  private String paymentMethod;
}
