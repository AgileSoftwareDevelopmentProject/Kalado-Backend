package com.kalado.common.feign.product;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ProductStatusUpdateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "product-service", path = "/products")
public interface ProductApi {

    @PostMapping()
    ProductDto createProduct(
            @RequestBody ProductDto productDto
    );

    @PutMapping(value = "/{id}")
    ProductDto updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDto productDto
    );

    @PutMapping("/delete/{id}")
    void deleteProduct(
            @PathVariable Long id,
            @RequestParam("userId") Long sellerId
    );

    @PutMapping(value = "/status/{id}")
    ProductDto updateProductStatus(
            @PathVariable Long id,
            @RequestParam("userId") Long userId,
            @RequestBody ProductStatusUpdateDto statusUpdate
    );

    @GetMapping("/seller/{sellerId}")
    List<ProductDto> getSellerProducts(@PathVariable Long sellerId);

    @GetMapping("/category/{category}")
    List<ProductDto> getProductsByCategory(@PathVariable String category);

    @GetMapping("/{id}")
    ProductDto getProduct(@PathVariable Long id);
}