package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.repository.CartRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

  private final CartRepository cartRepository;

  public CartServiceImpl(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
  }

  @Override
  public ResponseEntity<String> addItem(CartDto cartDto) {
    Cart cart =
        cartRepository.findAllByUserIdAndProductId(cartDto.getUserId(), cartDto.getProductId());
    if (cart == null) {
      cart =
          Cart.builder()
              .userId(cartDto.getUserId())
              .productId(cartDto.getProductId())
              .quantity(cartDto.getQuantity())
              .build();
    } else {
      cart.setQuantity(cart.getQuantity() + 1);
    }
    cartRepository.save(cart);
    return ResponseEntity.ok("Product is added in the cart");
  }

  @Override
  public ResponseEntity<String> updateQuantity(Long userId, Long productId) {
    Cart cart = cartRepository.findAllByUserIdAndProductId(userId, productId);
    cart.setQuantity(cart.getQuantity() + 1);

    cartRepository.save(cart);
    return ResponseEntity.ok("Product quantity is updated in the cart");
  }

  @Override
  public ResponseEntity<List<CartDto>> getCartDetails(Long userId) {
    List<Cart> items = cartRepository.findAllByUserId(userId);
    List<CartDto> cartDtoList =
        items.stream()
            .map(
                item ->
                    CartDto.builder()
                        .productId(item.getProductId())
                        .userId(item.getUserId())
                        .quantity(item.getQuantity())
                        .build())
            .toList();
    return ResponseEntity.ok(cartDtoList);
  }

  @Override
  public ResponseEntity<String> deleteCart(Long userId) {
    cartRepository.deleteByUserId(userId);
    return ResponseEntity.ok("Cart is cleared!");
  }
}
