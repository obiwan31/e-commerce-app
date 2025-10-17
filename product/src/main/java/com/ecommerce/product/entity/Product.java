package com.ecommerce.product.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(
    name = "products",
    uniqueConstraints = @UniqueConstraint(name = "uk_product_name", columnNames = "name"))
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String name;

  private String description;

  @Column(nullable = false)
  private BigDecimal price;

  @Column(nullable = false)
  private Integer quantity;

  private String category;
}
