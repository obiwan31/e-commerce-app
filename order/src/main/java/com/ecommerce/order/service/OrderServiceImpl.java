package com.ecommerce.order.service;

import com.ecommerce.order.client.CartServiceClient;
import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.OrderItemDto;
import com.ecommerce.order.dto.OrderRequestDto;
import com.ecommerce.order.dto.OrderResponseDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.model.CartDto;
import com.ecommerce.order.model.ProductDto;
import com.ecommerce.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final CartServiceClient cartServiceClient;
  private final ProductServiceClient productServiceClient;

  public OrderServiceImpl(
      OrderRepository orderRepository,
      CartServiceClient cartServiceClient,
      ProductServiceClient productServiceClient) {
    this.orderRepository = orderRepository;
    this.cartServiceClient = cartServiceClient;
    this.productServiceClient = productServiceClient;
  }

  @Override
  public ResponseEntity<OrderResponseDto> placeOrder(OrderRequestDto request) {
    List<CartDto> cartItems = cartServiceClient.getCartDetails(request.getUserId());

    if (cartItems == null || cartItems.isEmpty()) {
      return ResponseEntity.badRequest().body(null); // or custom error
    }

    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    for (CartDto cartItem : cartItems) {
      ProductDto product = productServiceClient.getProduct(cartItem.getProductId());

      if (product.getQuantity() < cartItem.getQuantity()) {
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
      }

      product.setQuantity(product.getQuantity() - cartItem.getQuantity());
      productServiceClient.updateProduct(cartItem.getProductId(), product);

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
    cartServiceClient.deleteCart(request.getUserId());

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

    OrderResponseDto responseDto =
        OrderResponseDto.builder()
            .orderId(savedOrder.getId())
            .status(savedOrder.getStatus())
            .total(savedOrder.getTotal())
            .items(itemDtos)
            .build();

    return ResponseEntity.ok(responseDto);
  }

  @Override
  public ResponseEntity<OrderResponseDto> getOrder(Long orderId) {
    Optional<Order> order = orderRepository.findById(orderId);
    if (order.isPresent()) {
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

      OrderResponseDto orderResponseDto =
          OrderResponseDto.builder()
              .orderId(orderData.getId())
              .status(orderData.getStatus())
              .total(orderData.getTotal())
              .items(orderItemDtoList)
              .build();

      return ResponseEntity.ok(orderResponseDto);
    } else {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
  }

  @Override
  public ResponseEntity<List<OrderSummaryDto>> getOrdersByUser(Long userId) {
    List<Order> orders = orderRepository.findByUserId(userId);

    if (orders.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    List<OrderSummaryDto> summaries =
        orders.stream()
            .map(
                order ->
                    OrderSummaryDto.builder()
                        .orderId(order.getId())
                        .status(order.getStatus())
                        .total(order.getTotal())
                        .createdAt(order.getCreatedAt())
                        .build())
            .toList();
    return ResponseEntity.ok(summaries);
  }
}
