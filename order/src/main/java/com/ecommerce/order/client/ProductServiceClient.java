package com.ecommerce.order.client;

import com.ecommerce.order.model.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product")
public interface ProductServiceClient {

  @GetMapping(value = "/products/{productId}", consumes = "application/json")
  ProductDto getProduct(@PathVariable("productId") Long productId);

  @PostMapping( value ="/products/{productId}")
  void updateProduct(@PathVariable("productId") Long productId, @RequestBody ProductDto product);

  //  private final RestClient restClient;
  //
  //  public ProductServiceClient(RestClient restClient) {
  //    this.restClient = restClient;
  //  }
  //
  //  public ProductDto getProduct(Long productId) {
  //    return restClient
  //        .get()
  //        .uri("http://localhost:8081/products/{productId}", productId)
  //        .retrieve()
  //        .body(ProductDto.class);
  //  }
  //
  //  public void updateProduct(Long productId, ProductDto product) {
  //    restClient
  //        .patch()
  //        .uri("http://localhost:8081/products/{productId}", productId)
  //        .body(product)
  //        .retrieve()
  //        .toBodilessEntity();
  //  }
}
