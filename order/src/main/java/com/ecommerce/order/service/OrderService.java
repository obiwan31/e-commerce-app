package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequestDto;
import com.ecommerce.order.dto.OrderResponseDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface OrderService {

  ResponseEntity<OrderResponseDto> placeOrder(OrderRequestDto request);

  ResponseEntity<OrderResponseDto> getOrder(Long orderId);

  ResponseEntity<List<OrderSummaryDto>> getOrdersByUser(Long userId);
}
