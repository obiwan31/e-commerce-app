package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.repository.CartRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

  private final CartRepository cartRepository;

  public CartServiceImpl(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
  }

  @Override
  public Long addItem(CartDto cartDto, Long userId) {
    Cart cart = cartRepository.findAllByUserIdAndProductId(userId, cartDto.getProductId());

    if (cart == null) {
      cart =
          Cart.builder()
              .userId(userId)
              .productId(cartDto.getProductId())
              .quantity(cartDto.getQuantity())
              .build();
    } else {
      cart.setQuantity(cart.getQuantity() + 1);
    }
    Cart savedCart = cartRepository.save(cart);
    return savedCart.getId();
  }

  @Override
  public Long updateQuantity(Long userId, Long productId) {
    Cart cart = cartRepository.findAllByUserIdAndProductId(userId, productId);
    cart.setQuantity(cart.getQuantity() + 1);
    Cart savedCart = cartRepository.save(cart);
    return savedCart.getId();
  }

  @Override
  public List<CartDto> getCartDetails(Long userId) {
    List<Cart> items = cartRepository.findAllByUserId(userId);
    return items.stream()
        .map(
            item ->
                CartDto.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .build())
        .toList();
  }

  @Override
  public void deleteCart(Long userId) {
    cartRepository.deleteByUserId(userId);
  }
}
