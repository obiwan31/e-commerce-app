package com.ecommerce.cart.repository;

import com.ecommerce.cart.entity.Cart;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

  List<Cart> findAllByUserId(Long userId);

  Cart findAllByUserIdAndProductId(Long userId, Long productId);

  @Transactional
  @Modifying
  @Query("DELETE FROM Cart c WHERE c.userId = :userId")
  void deleteByUserId(@Param("userId") Long userId);
}
