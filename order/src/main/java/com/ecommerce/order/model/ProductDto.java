package com.ecommerce.order.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductDto {
  private String name;
  private String description;
  private BigDecimal price;
  private Integer quantity;
  private String category;
}
