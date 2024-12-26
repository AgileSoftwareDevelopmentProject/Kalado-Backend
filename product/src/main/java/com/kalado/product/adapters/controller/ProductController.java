package com.kalado.product.adapters.controller;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ProductStatusUpdateDto;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.common.feign.product.ProductApi;
import com.kalado.product.application.service.ProductService;
import com.kalado.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController implements ProductApi {
  private final ProductService productService;
  private final ProductMapper productMapper;

  @Override
  public ProductDto createProduct(
          @RequestBody ProductDto productDto) {
    Product product = productMapper.toProduct(productDto);
    Product createdProduct = productService.createProduct(product, null);
    return productMapper.toResponseDto(createdProduct);
  }

  @Override
  public ProductDto updateProduct(
      @PathVariable Long id,
      @RequestPart("product") ProductDto productDto) {
    Product product = productMapper.toProduct(productDto);
    Product updatedProduct = productService.updateProduct(id, product, null);
    return productMapper.toResponseDto(updatedProduct);
  }

  @Override
  public void deleteProduct(@PathVariable Long id, @RequestParam("userId") Long userId) {
    productService.deleteProduct(id, userId);
  }

  @Override
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
  public List<ProductDto> getSellerProducts(@PathVariable Long sellerId) {
    List<Product> products = productService.getProductsBySeller(sellerId);
    return products.stream().map(productMapper::toResponseDto).collect(Collectors.toList());
  }

  @Override
  public List<ProductDto> getProductsByCategory(@PathVariable String category) {
    List<Product> products = productService.getProductsByCategory(category);
    return products.stream().map(productMapper::toResponseDto).collect(Collectors.toList());
  }

  @Override
  public ProductDto getProduct(@PathVariable Long id) {
    Product product = productService.getProduct(id);
    return productMapper.toResponseDto(product);
  }
}
