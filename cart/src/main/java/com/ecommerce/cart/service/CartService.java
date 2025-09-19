package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.CartDto;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface CartService {

  ResponseEntity<String> addItem(CartDto cartDto);

  ResponseEntity<String> updateQuantity(Long userId, Long productId);

  ResponseEntity<List<CartDto>> getCartDetails(Long userId);

  ResponseEntity<String> deleteCart(Long userId);
}
