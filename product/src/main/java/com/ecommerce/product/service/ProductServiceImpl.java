package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepo;

  public ProductServiceImpl(ProductRepository productRepo) {
    this.productRepo = productRepo;
  }

  @Override
  public ResponseEntity<String> createProduct(ProductDto productRequest) {
    Product product =
        Product.builder()
            .name(productRequest.getName())
            .description(productRequest.getDescription())
            .quantity(productRequest.getQuantity())
            .price(productRequest.getPrice())
            .category(productRequest.getCategory())
            .build();
    productRepo.save(product);
    return ResponseEntity.ok("Product is added");
  }

  @Override
  public ResponseEntity<String> updateProduct(Long id, ProductDto productRequest) {
    Optional<Product> optionalProduct = productRepo.findById(id);
    if (optionalProduct.isPresent()) {
      Product product = optionalProduct.get();
      product.setDescription(productRequest.getDescription());
      product.setPrice(productRequest.getPrice());
      product.setCategory(productRequest.getCategory());
      product.setQuantity(productRequest.getQuantity());
      productRepo.save(product);
      return ResponseEntity.ok("Product is updated");
    } else {
      return ResponseEntity.ok("Product does not exist");
    }
  }

  @Override
  public ResponseEntity<ProductDto> getProduct(Long id) {
    Optional<Product> optionalProduct = productRepo.findById(id);
    if (optionalProduct.isPresent()) {
      ProductDto dto = ProductDto.build(optionalProduct.get());
      return ResponseEntity.ok(dto);
    } else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    List<Product> products = productRepo.findAll();
    List<ProductDto> result =
        products.stream().filter(Objects::nonNull).map(ProductDto::build).toList();
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<String> deleteProduct(Long id) {
    productRepo.deleteById(id);
    return ResponseEntity.ok("Product has been removed!");
  }
}
