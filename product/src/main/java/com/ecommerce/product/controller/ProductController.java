package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.service.ProductService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @PostMapping
  public ResponseEntity<String> createProduct(@RequestBody ProductDto productRequest) {
    Long productId = productService.createProduct(productRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body("Product created with ID: " + productId);
  }

  @PostMapping("/{productId}")
  public ResponseEntity<String> updateProduct(
      @PathVariable("productId") Long id, @RequestBody ProductDto productRequest) {
    Long productId = productService.updateProduct(id, productRequest);
    if (productId != null) {
      return ResponseEntity.ok("Product updated successfully with ID: " + productId);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
    }
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductDto> getProduct(@PathVariable("productId") Long id) {
    ProductDto productDto = productService.getProductById(id);
    if (productDto != null) {
      return ResponseEntity.ok(productDto);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    List<ProductDto> productDto = productService.getAllProducts();
    return ResponseEntity.ok(productDto);
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<String> deleteProduct(@PathVariable("productId") Long id) {
    try {
      productService.deleteProduct(id);
      return ResponseEntity.ok("Product deleted successfully with ID: " + id);
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage());
      return ResponseEntity.notFound().build();
    }
  }
}
