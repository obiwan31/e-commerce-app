package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDto;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface ProductService {

  ResponseEntity<String> createProduct(ProductDto productRequest);

  ResponseEntity<String> updateProduct(Long id, ProductDto productRequest);

  ResponseEntity<ProductDto> getProduct(Long id);

  ResponseEntity<List<ProductDto>> getAllProducts();

  ResponseEntity<String> deleteProduct(Long id);
}
