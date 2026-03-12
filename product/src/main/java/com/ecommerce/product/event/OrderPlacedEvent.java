package com.ecommerce.product.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPlacedEvent {
  @NotBlank(message = "eventId is required")
  private String eventId;

  @NotBlank(message = "eventType is required")
  @Pattern(regexp = "ORDER_PLACED", message = "eventType must be ORDER_PLACED")
  private String eventType;

  @NotNull(message = "occurredAt is required")
  private Instant occurredAt;

  @NotNull(message = "orderId is required")
  private Long orderId;

  @NotNull(message = "userId is required")
  private Long userId;

  @NotEmpty(message = "items are required")
  @Valid
  private List<OrderPlacedItemEvent> items;
}
