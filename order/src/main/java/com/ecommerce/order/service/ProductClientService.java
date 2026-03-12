package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.exception.DownstreamServiceException;
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

  public ProductDto fallbackGetProduct(Long productId, Throwable throwable) {
    LOGGER.warn(
        "Fallback triggered for productId={} due to: {}", productId, throwable.getMessage());
    throw new DownstreamServiceException("Product service is unavailable");
  }
}
