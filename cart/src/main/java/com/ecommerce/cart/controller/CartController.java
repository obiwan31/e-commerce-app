package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.exception.BadRequestException;
import com.ecommerce.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@Tag(name = "Cart", description = "Shopping cart endpoints")
public class CartController {
  private static final String USER_ID = "x-user-id";

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @PostMapping("/items/add")
  @Operation(summary = "Add product to cart")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Item added to cart"),
    @ApiResponse(responseCode = "400", description = "Missing or invalid x-user-id header")
  })
  public ResponseEntity<String> addItem(
      @Valid @RequestBody CartDto cartDto, @RequestHeader Map<String, String> headers) {
    Long userId = extractUserId(headers);
    Long cartId = cartService.addItem(cartDto, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body("Product added in cart: " + cartId);
  }

  @PutMapping("/items/update")
  @Operation(summary = "Update product quantity in cart")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Cart updated"),
    @ApiResponse(responseCode = "400", description = "Missing or invalid x-user-id header")
  })
  public ResponseEntity<String> updateQuantity(
      @RequestParam Long productId, @RequestHeader Map<String, String> headers) {
    Long userId = extractUserId(headers);
    Long cartId = cartService.updateQuantity(userId, productId);
    return ResponseEntity.ok("Product quantity is updated in the cart with ID: " + cartId);
  }

  @GetMapping("/items")
  @Operation(summary = "Get cart details for current user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Cart retrieved"),
    @ApiResponse(responseCode = "400", description = "Missing or invalid x-user-id header")
  })
  public ResponseEntity<List<CartDto>> getCartDetails(@RequestHeader Map<String, String> headers) {
    Long userId = extractUserId(headers);
    List<CartDto> cartDtos = cartService.getCartDetails(userId);
    return ResponseEntity.ok(cartDtos);
  }

  @DeleteMapping("/items")
  @Operation(summary = "Clear current user cart")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Cart cleared"),
    @ApiResponse(responseCode = "400", description = "Missing or invalid x-user-id header")
  })
  public ResponseEntity<String> deleteCart(@RequestHeader Map<String, String> headers) {
    Long userId = extractUserId(headers);
    cartService.deleteCart(userId);
    return ResponseEntity.ok("Cart is cleared!");
  }

  private Long extractUserId(Map<String, String> headers) {
    String userIdHeader = headers.get(USER_ID);
    if (userIdHeader == null || userIdHeader.isBlank()) {
      throw new BadRequestException("Missing required header: x-user-id");
    }
    try {
      return Long.valueOf(userIdHeader);
    } catch (NumberFormatException ex) {
      throw new BadRequestException("Invalid x-user-id header value");
    }
  }
}
