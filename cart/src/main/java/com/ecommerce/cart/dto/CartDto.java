package com.ecommerce.cart.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartDto {
  private Long productId;
  private Integer quantity;
}
