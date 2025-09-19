package com.ecommerce.order.client;

import com.ecommerce.order.model.CartDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart")
public interface CartServiceClient {

  @GetMapping("/cart/items/{usedId}")
  public List<CartDto> getCartDetails(@PathVariable("usedId") Long userId);

  @DeleteMapping("/cart/items/{usedId}")
  void deleteCart(@PathVariable("usedId") Long userId);

  //  private final RestClient restClient;
  //
  //  public CartServiceClient(RestClient restClient) {
  //    this.restClient = restClient;
  //  }
  //
  //  public List<CartDto> getCartDetails(Long userId) {
  //    return restClient
  //        .get()
  //        .uri("http://localhost:8082/cart/items/{userId}", userId)
  //        .retrieve()
  //        .body(new ParameterizedTypeReference<>() {});
  //  }
  //
  //  public void deleteCart(Long userId) {
  //    restClient
  //        .delete()
  //        .uri("http://localhost:8082/cart/items/{userId}", userId)
  //        .retrieve()
  //        .toBodilessEntity();
  //  }
}
