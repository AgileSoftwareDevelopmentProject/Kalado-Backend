package com.kalado.gateway.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ProductStatusUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
  private final ProductApi productApi;
  private final ObjectMapper objectMapper;  // Will be used to convert between JSON and objects

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Authentication(userId = "#userId")
  public ProductDto createProduct(
          Long userId,
          @RequestParam("product") String productJson,  // Changed from ProductDto to String
          @RequestParam(value = "images", required = false) List<MultipartFile> images) {
    try {
      // Deserialize the JSON string to a ProductDto
      ProductDto productDto = objectMapper.readValue(productJson, ProductDto.class);
      productDto.setSellerId(userId);

      // Log the request for debugging
      log.debug("Creating product with data: {} and {} images", productDto,
              images != null ? images.size() : 0);

      // Forward to the product service
      return productApi.createProduct(productJson, images);
    } catch (Exception e) {
      log.error("Error creating product: {}", e.getMessage());
      throw new CustomException(ErrorCode.BAD_REQUEST, "Error processing request: " + e.getMessage());
    }
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Authentication(userId = "#userId")
  public ProductDto updateProduct(
          Long userId,
          @PathVariable Long id,
          @RequestParam("product") String productJson,  // Changed from ProductDto to String
          @RequestParam(value = "images", required = false) List<MultipartFile> images) {
    try {
      // Deserialize the JSON string to a ProductDto
      ProductDto productDto = objectMapper.readValue(productJson, ProductDto.class);
      productDto.setSellerId(userId);

      // Log the request for debugging
      log.debug("Updating product {} with data: {} and {} images", id, productDto,
              images != null ? images.size() : 0);

      // Forward to the product service
      return productApi.updateProduct(id, productJson, images);
    } catch (Exception e) {
      log.error("Error updating product: {}", e.getMessage());
      throw new CustomException(ErrorCode.BAD_REQUEST, "Error processing request: " + e.getMessage());
    }
  }

  @PutMapping("/delete/{id}")
  @Authentication(userId = "#userId")
  public void deleteProduct(Long userId, @PathVariable Long id) {
    productApi.deleteProduct(id, userId);
  }

  @PutMapping(value = "/status/{id}")
  @Authentication(userId = "#userId")
  public ProductDto updateProductStatus(
          Long userId,
          @PathVariable Long id,
          @RequestBody ProductStatusUpdateDto statusUpdate) {
    if (statusUpdate == null || statusUpdate.getStatus() == null) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Status cannot be null");
    }
    return productApi.updateProductStatus(id, userId, statusUpdate);
  }

  @GetMapping("/seller")
  @Authentication(userId = "#userId")
  public List<ProductDto> getSellerProducts(Long userId) {
    return productApi.getSellerProducts(userId);
  }

  @GetMapping("/category/{category}")
  public List<ProductDto> getProductsByCategory(@PathVariable String category) {
    return productApi.getProductsByCategory(category);
  }

  @GetMapping("/{id}")
  public ProductDto getProduct(@PathVariable Long id) {
    return productApi.getProduct(id);
  }
}