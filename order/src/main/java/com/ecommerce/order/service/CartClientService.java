package com.ecommerce.order.service;

import com.ecommerce.order.client.CartServiceClient;
import com.ecommerce.order.model.CartDto;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
  public List<CartDto> getCartDetails(Map<String, String> headers) {
    return cartServiceClient.getCartDetails(headers);
  }

  @RateLimiter(name = "cartRateLimiter", fallbackMethod = "fallbackDeleteCart")
  public void deleteCart(Map<String, String> headers) {
    cartServiceClient.deleteCart(headers);
  }

  public List<CartDto> fallbackGetCart(Map<String, String> headers, Throwable t) {
    LOGGER.error("Fallback for getCartDetails triggered due to {}", t.getMessage());
    return Collections.emptyList();
  }

  public void fallbackDeleteCart(Map<String, String> headers, Throwable t) {
    LOGGER.warn("Fallback for deleteCart triggered due to {}", t.getMessage());
    // Optionally, you could throw a custom exception or alert here
  }
}
