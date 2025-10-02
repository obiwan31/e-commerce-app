package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.model.ProductDto;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductClientService {
  private final Logger LOGGER = LoggerFactory.getLogger(ProductClientService.class);

  private final ProductServiceClient productServiceClient;

  public ProductClientService(ProductServiceClient productServiceClient) {
    this.productServiceClient = productServiceClient;
  }

  @RateLimiter(name = "productRateLimiter", fallbackMethod = "fallbackGetProduct")
  public ProductDto getProduct(Long productId) {
    return productServiceClient.getProduct(productId);
  }

  @RateLimiter(name = "productRateLimiter", fallbackMethod = "fallbackUpdateProduct")
  public void updateProduct(Long productId, ProductDto product) {
    productServiceClient.updateProduct(productId, product);
  }

  public ProductDto fallbackGetProduct(Long productId, Throwable throwable) {
    LOGGER.warn(
        "Fallback triggered for productId={} due to: {}", productId, throwable.getMessage());
    return ProductDto.builder().build();
  }

  public ProductDto fallbackUpdateProduct(Long productId, Throwable throwable) {
    LOGGER.warn(
        "Fallback triggered for update productId={} due to: {}", productId, throwable.getMessage());
    return ProductDto.builder().build();
  }
}
