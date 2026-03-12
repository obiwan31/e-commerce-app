package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderItemDto;
import com.ecommerce.order.dto.OrderRequestDto;
import com.ecommerce.order.dto.OrderResponseDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.event.OrderPlacedEvent;
import com.ecommerce.order.event.OrderPlacedItemEvent;
import com.ecommerce.order.exception.BadRequestException;
import com.ecommerce.order.exception.DownstreamServiceException;
import com.ecommerce.order.exception.NotFoundException;
import com.ecommerce.order.model.CartDto;
import com.ecommerce.order.model.ProductDto;
import com.ecommerce.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);
  private static final String ORDER_PLACED_TOPIC = "order.placed.v1";
  private static final String USER_ID_HEADER = "x-user-id";
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  private final OrderRepository orderRepository;
  private final CartClientService cartClientService;
  private final ProductClientService productClientService;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final Validator validator;

  public OrderServiceImpl(
      OrderRepository orderRepository,
      CartClientService cartClientService,
      ProductClientService productClientService,
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      Validator validator) {
    this.orderRepository = orderRepository;
    this.cartClientService = cartClientService;
    this.productClientService = productClientService;
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.validator = validator;
  }

  @RateLimiter(name = "orderRateLimiter", fallbackMethod = "fallback")
  @Override
  public OrderResponseDto placeOrder(OrderRequestDto request, Map<String, String> headers) {
    Long userId = extractUserId(headers);
    validateRequestUser(userId, request.getUserId());

    List<CartDto> cartItems = cartClientService.getCartDetails(headers);

    if (cartItems == null || cartItems.isEmpty()) {
      throw new BadRequestException("Cart is empty");
    }

    List<OrderItem> orderItems = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    for (CartDto cartItem : cartItems) {
      ProductDto product = productClientService.getProduct(cartItem.getProductId());

      if (product == null || product.getQuantity() == null || product.getName() == null) {
        throw new DownstreamServiceException(
            "Unable to fetch product details for productId=" + cartItem.getProductId());
      }

      if (product.getQuantity() < cartItem.getQuantity()) {
        throw new BadRequestException(product.getName() + " is out of stock");
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
    order.setUserId(userId);
    order.setStatus("PENDING");
    order.setTotal(total);
    order.setCreatedAt(LocalDateTime.now());
    order.setOrderItems(orderItems);
    orderItems.forEach(orderItem -> orderItem.setOrder(order));

    Order savedOrder = orderRepository.save(order);
    String correlationId = headers.get(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = headers.get("x-correlation-id");
    }
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = MDC.get("correlationId");
    }
    publishOrderPlacedEvent(savedOrder, correlationId);
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

  private void publishOrderPlacedEvent(Order order, String correlationId) {
    List<OrderPlacedItemEvent> items =
        order.getOrderItems().stream()
            .map(
                item ->
                    OrderPlacedItemEvent.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
            .toList();

    OrderPlacedEvent event =
        OrderPlacedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("ORDER_PLACED")
            .occurredAt(Instant.now())
            .orderId(order.getId())
            .userId(order.getUserId())
            .items(items)
            .build();
    validateEventSchema(event);

    String payload;
    try {
      payload = objectMapper.writeValueAsString(event);
    } catch (Exception ex) {
      throw new DownstreamServiceException("Unable to serialize order event");
    }

    ProducerRecord<String, String> producerRecord =
        new ProducerRecord<>(ORDER_PLACED_TOPIC, String.valueOf(order.getId()), payload);
    if (correlationId != null && !correlationId.isBlank()) {
      producerRecord
          .headers()
          .add(new RecordHeader(CORRELATION_ID_HEADER, correlationId.getBytes(StandardCharsets.UTF_8)));
    }

    kafkaTemplate
        .send(producerRecord)
        .whenComplete(
            (result, ex) -> {
              if (ex == null) {
                LOGGER.info("Published ORDER_PLACED event for orderId={}", order.getId());
              } else {
                LOGGER.error("Failed to publish ORDER_PLACED event for orderId={}", order.getId(), ex);
              }
            });
  }

  private void validateEventSchema(OrderPlacedEvent event) {
    List<String> violations =
        validator.validate(event).stream()
            .map(ConstraintViolation::getMessage)
            .sorted()
            .toList();
    if (!violations.isEmpty()) {
      throw new DownstreamServiceException(
          "Order event schema validation failed: " + String.join("; ", violations));
    }
  }

  public OrderResponseDto fallback(
      OrderRequestDto request, Map<String, String> headers, Throwable throwable) {
    LOGGER.warn("Fallback triggered for placeOrder due to {}", throwable.getMessage());
    throw new DownstreamServiceException("Order service is busy. Please try again");
  }

  @Override
  public OrderResponseDto getOrder(Long orderId) {
    Optional<Order> order = orderRepository.findById(orderId);
    if (order.isEmpty()) {
      throw new NotFoundException("Order not found: " + orderId);
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

  private Long extractUserId(Map<String, String> headers) {
    String userId = headers.get(USER_ID_HEADER);
    if (userId == null || userId.isBlank()) {
      throw new BadRequestException("Missing required header: x-user-id");
    }
    try {
      return Long.valueOf(userId);
    } catch (NumberFormatException ex) {
      throw new BadRequestException("Invalid x-user-id header value");
    }
  }

  private void validateRequestUser(Long headerUserId, Long requestUserId) {
    if (requestUserId != null && !headerUserId.equals(requestUserId)) {
      throw new BadRequestException("x-user-id header does not match request userId");
    }
  }
}
