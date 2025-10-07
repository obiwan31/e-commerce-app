package com.ecommerce.order.client;

import com.ecommerce.order.model.CartDto;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "carts")
public interface CartServiceClient {

  @GetMapping("/carts/items")
  public List<CartDto> getCartDetails(@RequestHeader Map<String, String> headers);

  @DeleteMapping("/carts/items")
  void deleteCart(@RequestHeader Map<String, String> headers);
}
