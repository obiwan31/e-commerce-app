package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequestDto;
import com.ecommerce.order.dto.OrderResponseDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import com.ecommerce.order.service.OrderService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private static final String USER_ID = "x-user-id";

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ResponseEntity<OrderResponseDto> placeOrder(
      @RequestBody OrderRequestDto orderRequestDto, @RequestHeader Map<String, String> headers) {
    OrderResponseDto orderResponseDto = orderService.placeOrder(orderRequestDto, headers);
    return ResponseEntity.ok(orderResponseDto);
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
    OrderResponseDto orderResponseDto = orderService.getOrder(orderId);
    if (orderResponseDto != null) {
      return ResponseEntity.ok(orderResponseDto);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping
  public ResponseEntity<List<OrderSummaryDto>> getAllOrders(
      @RequestHeader Map<String, String> headers) {
    Long userId = Long.valueOf(headers.get(USER_ID));
    List<OrderSummaryDto> allOrders = orderService.getAllOrders(userId);
    return ResponseEntity.ok(allOrders);
  }
}
