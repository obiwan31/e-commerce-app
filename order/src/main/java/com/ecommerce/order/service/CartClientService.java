package com.ecommerce.order.service;

import com.ecommerce.order.client.CartServiceClient;
import com.ecommerce.order.model.CartDto;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CartClientService {
  private final Logger LOGGER = LoggerFactory.getLogger(CartClientService.class);

  private final CartServiceClient cartServiceClient;

  public CartClientService(CartServiceClient cartServiceClient) {
    this.cartServiceClient = cartServiceClient;
  }

  @RateLimiter(name = "cartRateLimiter", fallbackMethod = "fallbackGetCart")
  public List<CartDto> getCartDetails(Long userId) {
    return cartServiceClient.getCartDetails(userId);
  }

  @RateLimiter(name = "cartRateLimiter", fallbackMethod = "fallbackDeleteCart")
  public void deleteCart(Long userId) {
    cartServiceClient.deleteCart(userId);
  }

  public List<CartDto> fallbackGetCart(Long userId, Throwable t) {
    LOGGER.error(
        "Fallback for getCartDetails triggered for userId={} due to {}", userId, t.getMessage());
    return Collections.emptyList();
  }

  // Fallback for deleteCart (void return)
  public void fallbackDeleteCart(Long userId, Throwable t) {
    LOGGER.warn("Fallback for deleteCart triggered for userId={} due to {}", userId, t.getMessage());
    // Optionally, you could throw a custom exception or alert here
  }
}
