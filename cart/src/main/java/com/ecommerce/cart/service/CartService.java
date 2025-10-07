package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.CartDto;
import java.util.List;

public interface CartService {

  Long addItem(CartDto cartDto, Long userId);

  Long updateQuantity(Long userId, Long productId);

  List<CartDto> getCartDetails(Long userId);

  void deleteCart(Long userId);
}
