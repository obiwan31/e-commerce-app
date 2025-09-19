package com.ecommerce.order.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderItemDto {
  private Long productId;
  private String name;
  private BigDecimal price;
  private Integer quantity;
}
