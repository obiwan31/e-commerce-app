package com.ecommerce.product.dto;

import com.ecommerce.product.entity.Product;
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

  public static ProductDto build(Product product) {
    return ProductDto.builder()
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .category(product.getCategory())
        .quantity(product.getQuantity())
        .build();
  }
}
