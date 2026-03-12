package com.ecommerce.product.dto;

import com.ecommerce.product.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must be at most 100 characters")
  private String name;

  @NotBlank(message = "Description is required")
  @Size(max = 500, message = "Description must be at most 500 characters")
  private String description;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.01", message = "Price must be greater than 0")
  private BigDecimal price;

  @NotNull(message = "Quantity is required")
  @Min(value = 0, message = "Quantity cannot be negative")
  private Integer quantity;

  @NotBlank(message = "Category is required")
  @Size(max = 100, message = "Category must be at most 100 characters")
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
