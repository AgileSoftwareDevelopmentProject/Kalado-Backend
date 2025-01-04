package com.kalado.product.adapters.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ProductStatusUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.product.application.service.ProductService;
import com.kalado.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController implements ProductApi {
  private final ProductService productService;
  private final ProductMapper productMapper;
  private final ObjectMapper objectMapper;

  @Override
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ProductDto createProduct(
          @RequestPart(value = "product") String productJson,
          @RequestPart(value = "images", required = false) List<MultipartFile> images) {
    try {
      log.debug("Received product JSON: {}", productJson);
      log.debug("Received images count: {}", images != null ? images.size() : 0);

      // Parse the JSON string into a ProductDto
      ProductDto productDto = objectMapper.readValue(productJson, ProductDto.class);

      // Ensure we have a sellerId
      if (productDto.getSellerId() == null) {
        throw new CustomException(ErrorCode.BAD_REQUEST, "Seller ID is required");
      }

      // Convert DTO to domain model
      Product product = productMapper.toProduct(productDto);

      // The sellerId should now be properly set in the product
      Product createdProduct = productService.createProduct(product, images);

      return productMapper.toResponseDto(createdProduct);
    } catch (Exception e) {
      log.error("Error creating product", e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
              "Error creating product: " + e.getMessage());
    }
  }

  @Override
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ProductDto updateProduct(
          @PathVariable Long id,
          @RequestPart(value = "product", required = true) String productJson,
          @RequestPart(value = "images", required = false) List<MultipartFile> images) {
    try {
      // Log the received data for debugging
      log.debug("Updating product {}. Received JSON: {}", id, productJson);
      log.debug("Received images count: {}", images != null ? images.size() : 0);

      // Convert the JSON string to ProductDto
      ProductDto productDto = objectMapper.readValue(productJson, ProductDto.class);

      // Convert DTO to domain model and update the product
      Product product = productMapper.toProduct(productDto);
      Product updatedProduct = productService.updateProduct(id, product, images);

      // Convert back to DTO and return
      return productMapper.toResponseDto(updatedProduct);
    } catch (Exception e) {
      log.error("Error updating product", e);
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
              "Error updating product: " + e.getMessage());
    }
  }

  @Override
  @PutMapping("/delete/{id}")
  public void deleteProduct(
          @PathVariable Long id,
          @RequestParam("userId") Long userId) {
    productService.deleteProduct(id, userId);
  }

  @Override
  @PutMapping(value = "/status/{id}")
  public ProductDto updateProductStatus(
          @PathVariable Long id,
          @RequestParam("userId") Long userId,
          @RequestBody ProductStatusUpdateDto statusUpdate) {
    if (statusUpdate == null || statusUpdate.getStatus() == null) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Status cannot be null");
    }

    Product product = productService.updateProductStatus(id, statusUpdate.getStatus(), userId);
    return productMapper.toResponseDto(product);
  }

  @Override
  @GetMapping("/seller/{sellerId}")
  public List<ProductDto> getSellerProducts(@PathVariable Long sellerId) {
    List<Product> products = productService.getProductsBySeller(sellerId);
    return products.stream()
            .map(productMapper::toResponseDto)
            .collect(Collectors.toList());
  }

  @Override
  @GetMapping("/category/{category}")
  public List<ProductDto> getProductsByCategory(@PathVariable String category) {
    List<Product> products = productService.getProductsByCategory(category);
    return products.stream()
            .map(productMapper::toResponseDto)
            .collect(Collectors.toList());
  }

  @Override
  @GetMapping("/{id}")
  public ProductDto getProduct(@PathVariable Long id) {
    Product product = productService.getProduct(id);
    return productMapper.toResponseDto(product);
  }

  @Override
  @GetMapping("/all")
  public List<ProductDto> getAllProducts() {
    List<Product> products = productService.getAllProducts();
    return products.stream()
            .map(productMapper::toResponseDto)
            .collect(Collectors.toList());
  }
}