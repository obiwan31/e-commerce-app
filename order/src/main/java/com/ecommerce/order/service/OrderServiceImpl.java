package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderItemDto;
import com.ecommerce.order.dto.OrderRequestDto;
import com.ecommerce.order.dto.OrderResponseDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.model.CartDto;
import com.ecommerce.order.model.ProductDto;
import com.ecommerce.order.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

  private final OrderRepository orderRepository;
  private final CartClientService cartClientService;
  private final ProductClientService productClientService;
  private final KafkaTemplate<String, String> kafkaTemplate;

  public OrderServiceImpl(
      OrderRepository orderRepository,
      CartClientService cartClientService,
      ProductClientService productClientService,
      KafkaTemplate<String, String> kafkaTemplate) {
    this.orderRepository = orderRepository;
    this.cartClientService = cartClientService;
    this.productClientService = productClientService;
    this.kafkaTemplate = kafkaTemplate;
  }

  @RateLimiter(name = "orderRateLimiter", fallbackMethod = "fallback")
  @Override
  public OrderResponseDto placeOrder(OrderRequestDto request, Map<String, String> headers) {
    List<CartDto> cartItems = cartClientService.getCartDetails(headers);

    if (cartItems == null || cartItems.isEmpty()) {
      return OrderResponseDto.builder().message("Cart is Empty!").orderStatus("FAILED").build();
    }

    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    for (CartDto cartItem : cartItems) {
      ProductDto product = productClientService.getProduct(cartItem.getProductId());

      if (product.getQuantity() < cartItem.getQuantity()) {
        return OrderResponseDto.builder()
            .message(product.getName() + " is out of stock!")
            .orderStatus("FAILED")
            .build();
      }

      product.setQuantity(product.getQuantity() - cartItem.getQuantity());

      try {
        publishProductUpdate(cartItem.getProductId(), product);
      } catch (JsonProcessingException e) {
        return OrderResponseDto.builder().orderStatus("FAILED").build();
      }
      OrderItem orderItem = new OrderItem();
      orderItem.setProductId(cartItem.getProductId());
      orderItem.setPrice(product.getPrice());
      orderItem.setQuantity(cartItem.getQuantity());
      orderItem.setName(product.getName());
      orderItems.add(orderItem);

      total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
    }

    Order order = new Order();
    order.setUserId(request.getUserId());
    order.setStatus("PENDING");
    order.setTotal(total);
    order.setCreatedAt(LocalDateTime.now());
    order.setOrderItems(orderItems);
    orderItems.forEach(orderItem -> orderItem.setOrder(order));

    Order savedOrder = orderRepository.save(order);
    cartClientService.deleteCart(headers);

    List<OrderItemDto> itemDtos =
        orderItems.stream()
            .map(
                item ->
                    OrderItemDto.builder()
                        .productId(item.getProductId())
                        .name(item.getName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build())
            .toList();

    return OrderResponseDto.builder()
        .orderId(savedOrder.getId())
        .status(savedOrder.getStatus())
        .total(savedOrder.getTotal())
        .items(itemDtos)
        .build();
  }

  private void publishProductUpdate(Long productId, ProductDto product)
      throws JsonProcessingException {
    String jsonString = new ObjectMapper().writeValueAsString(product);
    kafkaTemplate
        .send("OrderEvent", productId.toString(), jsonString)
        .whenComplete(
            (result, ex) -> {
              if (ex == null) {
                LOGGER.info("Product update sent for ID {}", productId);
              } else {
                LOGGER.error("Failed to send product update for ID {}", productId, ex);
              }
            });
  }

  public OrderResponseDto fallback(
      OrderRequestDto request, Map<String, String> headers, Throwable throwable) {
    LOGGER.warn("Fallback triggered for placeOrder due to {}", throwable.getMessage());

    return OrderResponseDto.builder()
        .message("Service is busy. Please try again later.")
        .orderStatus("FAILED")
        .build();
  }

  @Override
  public OrderResponseDto getOrder(Long orderId) {
    Optional<Order> order = orderRepository.findById(orderId);
    if (order.isEmpty()) {
      return null;
    }

    Order orderData = order.get();
    List<OrderItem> orderItems = orderData.getOrderItems();

    List<OrderItemDto> orderItemDtoList =
        orderItems.stream()
            .map(
                orderItem ->
                    OrderItemDto.builder()
                        .name(orderItem.getName())
                        .productId(orderItem.getProductId())
                        .quantity(orderItem.getQuantity())
                        .price(orderItem.getPrice())
                        .build())
            .toList();

    return OrderResponseDto.builder()
        .orderId(orderData.getId())
        .status(orderData.getStatus())
        .total(orderData.getTotal())
        .items(orderItemDtoList)
        .build();
  }

  @Override
  public List<OrderSummaryDto> getAllOrders(Long userId) {
    List<Order> orders = orderRepository.findByUserId(userId);

    if (orders.isEmpty()) {
      return Collections.emptyList();
    }
    return orders.stream()
        .map(
            order ->
                OrderSummaryDto.builder()
                    .orderId(order.getId())
                    .status(order.getStatus())
                    .total(order.getTotal())
                    .createdAt(order.getCreatedAt())
                    .build())
        .toList();
  }
}
