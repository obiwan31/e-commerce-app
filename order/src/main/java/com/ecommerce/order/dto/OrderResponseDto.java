package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderResponseDto {
  private Long orderId;
  private String status;
  private BigDecimal total;
  private List<OrderItemDto> items;
}
