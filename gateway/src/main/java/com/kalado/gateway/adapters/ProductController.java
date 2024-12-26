package com.kalado.gateway.adapters;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ProductStatusUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.gateway.annotation.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/product")
@RequiredArgsConstructor
public class ProductController {
  private final ProductApi productApi;

  @PostMapping()
  @Authentication(userId = "#userId")
  public ProductDto createProduct(
          Long userId,
          @RequestBody ProductDto productDto) {
    productDto.setSellerId(userId);
    return productApi.createProduct(productDto);
  }

  @PutMapping("/{id}")
  @Authentication(userId = "#userId")
  public ProductDto updateProduct(
      Long userId,
      @PathVariable Long id,
      @RequestBody ProductDto productDto) {
    productDto.setSellerId(userId);
    return productApi.updateProduct(id, productDto);
  }

  @PutMapping("/delete/{id}")
  @Authentication(userId = "#userId")
  public void deleteProduct(Long userId, @PathVariable Long id) {
    productApi.deleteProduct(id, userId);
  }

  @PutMapping(value = "/status/{id}")
  @Authentication(userId = "#userId")
  public ProductDto updateProductStatus(
      Long userId, @PathVariable Long id, @RequestBody ProductStatusUpdateDto statusUpdate) {
    if (statusUpdate == null || statusUpdate.getStatus() == null) {
      throw new CustomException(ErrorCode.BAD_REQUEST, "Status cannot be null");
    }

    ProductDto updatedProduct = productApi.updateProductStatus(id, userId, statusUpdate);
    return updatedProduct;
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
