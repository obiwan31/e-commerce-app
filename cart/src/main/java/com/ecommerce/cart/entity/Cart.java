package com.ecommerce.cart.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carts")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cart {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private Long productId;

  private Integer quantity;
}
