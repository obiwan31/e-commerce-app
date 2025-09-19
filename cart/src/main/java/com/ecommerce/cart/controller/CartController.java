package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.service.CartService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @PostMapping("/items")
  public ResponseEntity<String> addItem(@RequestBody CartDto cartDto) {
    return cartService.addItem(cartDto);
  }

  @PutMapping("/items")
  public ResponseEntity<String> updateQuantity(
      @RequestParam Long userId, @RequestParam Long productId) {
    return cartService.updateQuantity(userId, productId);
  }

  @GetMapping("/items/{usedId}")
  public ResponseEntity<List<CartDto>> getCartDetails(@PathVariable("usedId") Long userId) {
    return cartService.getCartDetails(userId);
  }

  @DeleteMapping("/items/{usedId}")
  public ResponseEntity<String> deleteCart(@PathVariable("usedId")  Long userId) {
    return cartService.deleteCart(userId);
  }
}
