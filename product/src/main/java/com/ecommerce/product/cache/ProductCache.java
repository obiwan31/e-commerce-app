package com.ecommerce.product.cache;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class ProductCache {

  private final ProductRepository productRepo;

  public ProductCache(ProductRepository productRepo) {
    this.productRepo = productRepo;
  }

  @Cacheable(value = "product", key = "'product:' + #id")
  public Product getProduct(Long id) {
    Optional<Product> product = productRepo.findById(id);
    return product.orElse(null);
  }

  @Cacheable(value = "products", key = "'all'")
  public List<Product> getAllProducts() {
    return productRepo.findAll();
  }

  @CacheEvict(value = "product", key = "'product:' + #id")
  public void deleteById(Long id) {
    productRepo.deleteById(id);
  }

  @CachePut(value = "product", key = "'product:' + #product.id")
  public Product save(Product product) {
    return productRepo.save(product);
  }
}
