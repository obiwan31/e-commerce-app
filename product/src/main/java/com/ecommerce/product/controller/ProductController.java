package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.service.ProductService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @PostMapping
  public ResponseEntity<String> createProduct(@RequestBody ProductDto productRequest) {
    return productService.createProduct(productRequest);
  }

  @PostMapping("/{productId}")
  public ResponseEntity<String> updateProduct(
      @PathVariable("productId") Long id, @RequestBody ProductDto productRequest) {
    return productService.updateProduct(id, productRequest);
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductDto> getProduct(@PathVariable("productId") Long id) {
    return productService.getProduct(id);
  }

  @GetMapping
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    return productService.getAllProducts();
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<String> deleteProduct(@PathVariable("productId") Long id) {
    return productService.deleteProduct(id);
  }
}
