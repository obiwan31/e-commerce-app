package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequestDto;
import com.ecommerce.order.dto.OrderResponseDto;
import com.ecommerce.order.dto.OrderSummaryDto;
import com.ecommerce.order.exception.BadRequestException;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

  private static final String USER_ID = "x-user-id";

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  @Operation(summary = "Place a new order")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Order placed"),
    @ApiResponse(responseCode = "400", description = "Invalid request")
  })
  public ResponseEntity<OrderResponseDto> placeOrder(
      @Valid @RequestBody OrderRequestDto orderRequestDto,
      @RequestHeader Map<String, String> headers) {
    OrderResponseDto orderResponseDto = orderService.placeOrder(orderRequestDto, headers);
    return ResponseEntity.ok(orderResponseDto);
  }

  @GetMapping("/{orderId}")
  @Operation(summary = "Get order by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Order retrieved"),
    @ApiResponse(responseCode = "404", description = "Order not found")
  })
  public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
    return ResponseEntity.ok(orderService.getOrder(orderId));
  }

  @GetMapping
  @Operation(summary = "Get all orders for current user")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Orders retrieved"),
    @ApiResponse(responseCode = "400", description = "Missing or invalid x-user-id header")
  })
  public ResponseEntity<List<OrderSummaryDto>> getAllOrders(
      @RequestHeader Map<String, String> headers) {
    Long userId = extractUserId(headers);
    List<OrderSummaryDto> allOrders = orderService.getAllOrders(userId);
    return ResponseEntity.ok(allOrders);
  }

  private Long extractUserId(Map<String, String> headers) {
    String userIdHeader = headers.get(USER_ID);
    if (userIdHeader == null || userIdHeader.isBlank()) {
      throw new BadRequestException("Missing required header: x-user-id");
    }
    try {
      return Long.valueOf(userIdHeader);
    } catch (NumberFormatException ex) {
      throw new BadRequestException("Invalid x-user-id header value");
    }
  }
}
