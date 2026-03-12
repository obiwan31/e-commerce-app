package com.ecommerce.product.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPlacedItemEvent {
  @NotNull(message = "productId is required")
  private Long productId;

  @NotNull(message = "quantity is required")
  @Min(value = 1, message = "quantity must be greater than 0")
  private Integer quantity;
}
