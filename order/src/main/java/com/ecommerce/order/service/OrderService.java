package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderRequestDto;
import com.ecommerce.order.dto.OrderResponseDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import java.util.List;
import java.util.Map;

public interface OrderService {

  OrderResponseDto placeOrder(OrderRequestDto request, Map<String, String> headers);

  OrderResponseDto getOrder(Long orderId);

  List<OrderSummaryDto> getAllOrders(Long userId);
}
