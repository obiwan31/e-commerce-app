package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequestDto;
import com.ecommerce.order.dto.OrderResponseDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import com.ecommerce.order.service.OrderService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ResponseEntity<OrderResponseDto> placeOrder(@RequestBody OrderRequestDto orderRequestDto) {
    return orderService.placeOrder(orderRequestDto);
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
    return orderService.getOrder(orderId);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<OrderSummaryDto>> getOrdersByUser(@PathVariable Long userId) {
    return orderService.getOrdersByUser(userId);
  }
}
