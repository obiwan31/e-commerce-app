package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDto;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product catalog endpoints")
public class ProductController {
  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @PostMapping
  @Operation(summary = "Create a product")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Product created"),
    @ApiResponse(responseCode = "400", description = "Invalid input")
  })
  public ResponseEntity<String> createProduct(@Valid @RequestBody ProductDto productRequest) {
    Long productId = productService.createProduct(productRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body("Product created with ID: " + productId);
  }

  @PostMapping("/{productId}")
  @Operation(summary = "Update a product")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Product updated"),
    @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<String> updateProduct(
      @PathVariable("productId") Long id, @Valid @RequestBody ProductDto productRequest) {
    Long productId = productService.updateProduct(id, productRequest);
    return ResponseEntity.ok("Product updated successfully with ID: " + productId);
  }

  @GetMapping("/{productId}")
  @Operation(summary = "Get a product by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Product retrieved"),
    @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<ProductDto> getProduct(@PathVariable("productId") Long id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

  @GetMapping
  @Operation(summary = "Get all products")
  @ApiResponses({@ApiResponse(responseCode = "200", description = "Products retrieved")})
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    List<ProductDto> productDto = productService.getAllProducts();
    return ResponseEntity.ok(productDto);
  }

  @DeleteMapping("/{productId}")
  @Operation(summary = "Delete a product")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Product deleted"),
    @ApiResponse(responseCode = "404", description = "Product not found")
  })
  public ResponseEntity<String> deleteProduct(@PathVariable("productId") Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.ok("Product deleted successfully with ID: " + id);
  }
}
