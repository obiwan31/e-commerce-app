package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.service.CartService;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
public class CartController {
  private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);
  private static final String USER_ID = "x-user-id";

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @PostMapping("/items/add")
  public ResponseEntity<String> addItem(
      @RequestBody CartDto cartDto, @RequestHeader Map<String, String> headers) {
    Long userId = Long.valueOf(headers.get(USER_ID));
    Long cartId = cartService.addItem(cartDto, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body("Product added in cart: " + cartId);
  }

  @PutMapping("/items/update")
  public ResponseEntity<String> updateQuantity(
      @RequestParam Long productId, @RequestHeader Map<String, String> headers) {
    Long userId = Long.valueOf(headers.get(USER_ID));
    Long cartId = cartService.updateQuantity(userId, productId);
    if (cartId != null) {
      return ResponseEntity.ok("Product quantity is updated in the cart with ID: " + cartId);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
    }
  }

  @GetMapping("/items")
  public ResponseEntity<List<CartDto>> getCartDetails(@RequestHeader Map<String, String> headers) {
    Long userId = Long.valueOf(headers.get(USER_ID));
    List<CartDto> cartDtos = cartService.getCartDetails(userId);
    return ResponseEntity.ok(cartDtos);
  }

  @DeleteMapping("/items")
  public ResponseEntity<String> deleteCart(@RequestHeader Map<String, String> headers) {
    Long userId = Long.valueOf(headers.get(USER_ID));
    try {
      cartService.deleteCart(userId);
      return ResponseEntity.ok("Cart is cleared!");
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage());
      return ResponseEntity.notFound().build();
    }
  }
}
