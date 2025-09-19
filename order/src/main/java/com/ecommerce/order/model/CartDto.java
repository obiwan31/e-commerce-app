package com.ecommerce.order.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartDto {
  private Long userId;
  private Long productId;
  private Integer quantity;
}
