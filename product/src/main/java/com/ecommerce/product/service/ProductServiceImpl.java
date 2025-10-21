package com.ecommerce.product.service;

import com.ecommerce.product.cache.ProductCache;
import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final ProductRepository productRepo;
  private final ProductCache productCache;

  public ProductServiceImpl(ProductRepository productRepo, ProductCache productCache) {
    this.productRepo = productRepo;
    this.productCache = productCache;
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
    Product product = productCache.getProduct(id);
    if (product != null) {
      product.setDescription(productRequest.getDescription());
      product.setPrice(productRequest.getPrice());
      product.setCategory(productRequest.getCategory());
      product.setQuantity(productRequest.getQuantity());

      Product savedProduct = productCache.save(product);
      return savedProduct.getId();
    }
    return null;
  }

  @Override
  public ProductDto getProductById(Long id) {
    Product product = productCache.getProduct(id);
    return product != null ? ProductDto.build(product) : null;
  }

  @Override
  public List<ProductDto> getAllProducts() {
    List<Product> products = productCache.getAllProducts();
    return products.stream().filter(Objects::nonNull).map(ProductDto::build).toList();
  }

  @Override
  public void deleteProduct(Long id) {
    productCache.deleteById(id);
  }

  @KafkaListener(topics = "OrderEvent", groupId = "product-group")
  public void updateProductListener(ConsumerRecord<String, String> record, Acknowledgment ack)
      throws JsonProcessingException {
    ProductDto productDto = new ObjectMapper().readValue(record.value(), ProductDto.class);
    updateProduct(Long.valueOf(record.key()), productDto);
    LOGGER.info(
        "Received product update for productId={}, payload={}, topic={}, partition={}, offset={}",
        record.key(),
        record.value(),
        record.topic(),
        record.partition(),
        record.offset());
    ack.acknowledge();
  }
}
