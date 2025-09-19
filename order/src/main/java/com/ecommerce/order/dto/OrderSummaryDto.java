package com.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderSummaryDto {
  private Long orderId;
  private String status;
  private BigDecimal total;
  private LocalDateTime createdAt;
}
