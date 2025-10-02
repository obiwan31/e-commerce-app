package com.ecommerce.order.client;

import com.ecommerce.order.model.CartDto;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "carts")
public interface CartServiceClient {

  @GetMapping("/carts/items/{usedId}")
  public List<CartDto> getCartDetails(@PathVariable("usedId") Long userId);

  @DeleteMapping("/carts/items/{usedId}")
  void deleteCart(@PathVariable("usedId") Long userId);

}
