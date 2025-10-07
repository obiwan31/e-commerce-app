package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDto;
import java.util.List;

public interface ProductService {

  Long createProduct(ProductDto productRequest);

  Long updateProduct(Long id, ProductDto productRequest);

  ProductDto getProductById(Long id);

  List<ProductDto> getAllProducts();

  void deleteProduct(Long id);
}
