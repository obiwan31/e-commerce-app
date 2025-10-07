package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepo;

  public ProductServiceImpl(ProductRepository productRepo) {
    this.productRepo = productRepo;
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
    Optional<Product> optionalProduct = productRepo.findById(id);
    if (optionalProduct.isPresent()) {
      Product product = optionalProduct.get();
      product.setDescription(productRequest.getDescription());
      product.setPrice(productRequest.getPrice());
      product.setCategory(productRequest.getCategory());
      product.setQuantity(productRequest.getQuantity());

      Product savedProduct = productRepo.save(product);
      return savedProduct.getId();
    }
    return null;
  }

  @Override
  public ProductDto getProductById(Long id) {
    Optional<Product> optionalProduct = productRepo.findById(id);
    return optionalProduct.map(ProductDto::build).orElse(null);
  }

  @Override
  public List<ProductDto> getAllProducts() {
    List<Product> products = productRepo.findAll();
    return products.stream().filter(Objects::nonNull).map(ProductDto::build).toList();
  }

  @Override
  public void deleteProduct(Long id) {
    productRepo.deleteById(id);
  }
}
