package com.ecommerce.order.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedItemEvent {
  @NotNull(message = "productId is required")
  private Long productId;

  @NotNull(message = "quantity is required")
  @Min(value = 1, message = "quantity must be greater than 0")
  private Integer quantity;
}
