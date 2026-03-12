package com.ecommerce.product.service;

import com.ecommerce.product.cache.ProductCache;
import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.event.OrderPlacedEvent;
import com.ecommerce.product.event.OrderPlacedItemEvent;
import com.ecommerce.product.exception.BadRequestException;
import com.ecommerce.product.exception.ConflictException;
import com.ecommerce.product.exception.EventSchemaValidationException;
import com.ecommerce.product.exception.NotFoundException;
import com.ecommerce.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);
  private static final String ORDER_PLACED_TOPIC = "order.placed.v1";
  private static final String ORDER_PLACED_DLT_TOPIC = "order.placed.v1.dlt";
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  private final ProductRepository productRepo;
  private final ProductCache productCache;
  private final ObjectMapper objectMapper;
  private final Validator validator;
  private final KafkaIdempotencyService kafkaIdempotencyService;

  public ProductServiceImpl(
      ProductRepository productRepo,
      ProductCache productCache,
      ObjectMapper objectMapper,
      Validator validator,
      KafkaIdempotencyService kafkaIdempotencyService) {
    this.productRepo = productRepo;
    this.productCache = productCache;
    this.objectMapper = objectMapper;
    this.validator = validator;
    this.kafkaIdempotencyService = kafkaIdempotencyService;
  }

  @Override
  public Long createProduct(ProductDto productRequest) {
    Product product =
        Product.builder()
            .name(productRequest.getName())
            .description(productRequest.getDescription())
            .quantity(productRequest.getQuantity())
            .price(productRequest.getPrice())
            .category(productRequest.getCategory())
            .build();
    Product savedProduct = productRepo.save(product);
    return savedProduct.getId();
  }

  @Override
  public Long updateProduct(Long id, ProductDto productRequest) {
    if (productRequest.getQuantity() != null && productRequest.getQuantity() < 0) {
      throw new BadRequestException("Product quantity cannot be negative");
    }
    Product product = productCache.getProduct(id);
    if (product != null) {
      product.setDescription(productRequest.getDescription());
      product.setPrice(productRequest.getPrice());
      product.setCategory(productRequest.getCategory());
      product.setQuantity(productRequest.getQuantity());

      Product savedProduct = productCache.save(product);
      return savedProduct.getId();
    }
    throw new NotFoundException("Product not found: " + id);
  }

  @Override
  public ProductDto getProductById(Long id) {
    Product product = productCache.getProduct(id);
    if (product == null) {
      throw new NotFoundException("Product not found: " + id);
    }
    return ProductDto.build(product);
  }

  @Override
  public List<ProductDto> getAllProducts() {
    List<Product> products = productCache.getAllProducts();
    return products.stream().filter(Objects::nonNull).map(ProductDto::build).toList();
  }

  @Override
  public void deleteProduct(Long id) {
    if (productCache.getProduct(id) == null) {
      throw new NotFoundException("Product not found: " + id);
    }
    productCache.deleteById(id);
  }

  @KafkaListener(topics = ORDER_PLACED_TOPIC, groupId = "product-group")
  public void updateProductListener(ConsumerRecord<String, String> record, Acknowledgment ack)
      throws Exception {
    String correlationId = null;
    if (record.headers().lastHeader(CORRELATION_ID_HEADER) != null) {
      correlationId =
          new String(record.headers().lastHeader(CORRELATION_ID_HEADER).value(), StandardCharsets.UTF_8);
    }

    if (correlationId != null && !correlationId.isBlank()) {
      MDC.put("correlationId", correlationId);
    }
    try {
      OrderPlacedEvent event = objectMapper.readValue(record.value(), OrderPlacedEvent.class);
      validateEventSchema(event);

      if (kafkaIdempotencyService.isAlreadyProcessed(event.getEventId())) {
        LOGGER.info("Skipping already processed eventId={}", event.getEventId());
        ack.acknowledge();
        return;
      }
      if (!kafkaIdempotencyService.acquireProcessingLock(event.getEventId())) {
        LOGGER.info("Skipping in-progress duplicate eventId={}", event.getEventId());
        ack.acknowledge();
        return;
      }

      for (OrderPlacedItemEvent item : event.getItems()) {
        reduceInventory(item.getProductId(), item.getQuantity());
      }
      kafkaIdempotencyService.markProcessed(event.getEventId());

      LOGGER.info(
          "Processed ORDER_PLACED eventId={} orderId={} topic={} partition={} offset={}",
          event.getEventId(),
          event.getOrderId(),
          record.topic(),
          record.partition(),
          record.offset());
      ack.acknowledge();
    } catch (Exception ex) {
      tryReleaseProcessingLock(record.value());
      throw ex;
    } finally {
      MDC.remove("correlationId");
    }
  }

  @KafkaListener(topics = ORDER_PLACED_DLT_TOPIC, groupId = "product-group-dlt")
  public void handleOrderPlacedDlt(ConsumerRecord<String, String> record, Acknowledgment ack) {
    LOGGER.error(
        "Received DLT message topic={} partition={} offset={} key={} payload={}",
        record.topic(),
        record.partition(),
        record.offset(),
        record.key(),
        record.value());
    ack.acknowledge();
  }

  private void reduceInventory(Long productId, Integer quantity) {
    if (productId == null || quantity == null || quantity <= 0) {
      throw new BadRequestException("Invalid inventory reduction request");
    }

    Product product = productCache.getProduct(productId);
    if (product == null) {
      throw new NotFoundException("Product not found for inventory update: " + productId);
    }

    if (product.getQuantity() < quantity) {
      throw new ConflictException("Insufficient stock for productId=" + productId);
    }

    product.setQuantity(product.getQuantity() - quantity);
    productCache.save(product);
  }

  private void validateEventSchema(OrderPlacedEvent event) {
    List<String> violations =
        validator.validate(event).stream()
            .map(ConstraintViolation::getMessage)
            .sorted()
            .toList();
    if (!violations.isEmpty()) {
      throw new EventSchemaValidationException(
          "Order event schema validation failed: " + String.join("; ", violations));
    }
  }

  private void tryReleaseProcessingLock(String payload) {
    try {
      OrderPlacedEvent event = objectMapper.readValue(payload, OrderPlacedEvent.class);
      if (event.getEventId() != null && !event.getEventId().isBlank()) {
        kafkaIdempotencyService.releaseProcessingLock(event.getEventId());
      }
    } catch (Exception ignored) {
      // If payload can't be parsed we rely on retry/DLT path.
    }
  }
}
