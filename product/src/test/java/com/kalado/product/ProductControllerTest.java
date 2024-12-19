package com.kalado.product.adapters.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalado.common.Price;
import com.kalado.common.dto.ProductDto;
import com.kalado.common.dto.ProductStatusUpdateDto;
import com.kalado.common.enums.CurrencyUnit;
import com.kalado.common.enums.ErrorCode;
import com.kalado.common.exception.CustomException;
import com.kalado.product.adapters.dto.ProductCreateRequestDto;
import com.kalado.product.adapters.dto.ProductStatusUpdateRequestDto;
import com.kalado.product.application.service.ProductService;
import com.kalado.product.domain.model.Product;
import com.kalado.common.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ProductService productService;

  private ProductCreateRequestDto createRequestDto;
  private Product testProduct;
  //  private MockMultipartFile testImage;
  private final Long TEST_USER_ID = 1L;

  @BeforeEach
  void setUp() {
    createRequestDto =
        ProductCreateRequestDto.builder()
            .title("Test Product")
            .description("Test Description")
            .price(new Price(100000, CurrencyUnit.TOMAN))
            .category("Electronics")
            .productionYear(2023)
            .brand("Test Brand")
            .build();

    testProduct =
        Product.builder()
            .id(1L)
            .title(createRequestDto.getTitle())
            .description(createRequestDto.getDescription())
            .price(createRequestDto.getPrice())
            .category(createRequestDto.getCategory())
            .productionYear(createRequestDto.getProductionYear())
            .brand(createRequestDto.getBrand())
            .sellerId(TEST_USER_ID)
            .status(ProductStatus.ACTIVE)
            .build();

    //    testImage =
    //        new MockMultipartFile(
    //            "images",
    //            "test-image.jpg",
    //            MediaType.IMAGE_JPEG_VALUE,
    //            "test image content".getBytes());
  }

  @Test
  void createProduct_Success() throws Exception {
    when(productService.createProduct(any(Product.class), eq(null))).thenReturn(testProduct);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequestDto))
                .header("Authorization", "Bearer test-token")
                .requestAttr("userId", TEST_USER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testProduct.getId()))
        .andExpect(jsonPath("$.title").value(testProduct.getTitle()))
        .andExpect(jsonPath("$.sellerId").value(TEST_USER_ID));

    verify(productService).createProduct(any(Product.class), eq(null));
  }

  @Test
  void createProduct_WithNoImages_Success() throws Exception {
    ProductDto productDto =
        ProductDto.builder()
            .title("Test Product")
            .description("Test Description")
            .price(new Price(100000, CurrencyUnit.TOMAN))
            .category("Electronics")
            .productionYear(2023)
            .brand("Test Brand")
            .build();

    when(productService.createProduct(any(Product.class), eq(null))).thenReturn(testProduct);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto))
                .header("Authorization", "Bearer test-token")
                .requestAttr("userId", TEST_USER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testProduct.getId()))
        .andExpect(jsonPath("$.title").value(testProduct.getTitle()))
        .andExpect(jsonPath("$.description").value(testProduct.getDescription()));

    verify(productService).createProduct(any(Product.class), eq(null));
  }

  @Test
  void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
    createRequestDto.setTitle(""); // Invalid title

    when(productService.createProduct(any(), any()))
        .thenThrow(new CustomException(ErrorCode.BAD_REQUEST, "Title is required"));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequestDto))
                .header("Authorization", "Bearer test-token")
                .requestAttr("userId", TEST_USER_ID))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.getErrorCodeValue()))
        .andExpect(jsonPath("$.message").value("Title is required"));
  }

  //  @Test
  //  void createProduct_WithTooManyImages_ShouldReturnBadRequest() throws Exception {
  //    MockMultipartFile productFile =
  //        new MockMultipartFile(
  //            "product",
  //            "",
  //            MediaType.APPLICATION_JSON_VALUE,
  //            objectMapper.writeValueAsBytes(createRequestDto));
  //
  //    when(productService.createProduct(any(), any()))
  //        .thenThrow(new CustomException(ErrorCode.BAD_REQUEST, "Maximum 3 images allowed"));
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.multipart("/v1/products")
  //                .file(productFile)
  ////                .file(testImage)
  ////                .file(testImage)
  ////                .file(testImage)
  ////                .file(testImage) // 4 images
  //                .header("Authorization", "Bearer test-token")
  //                .requestAttr("userId", TEST_USER_ID))
  //        .andExpect(status().isBadRequest())
  //        .andExpect(jsonPath("$.message").value("Maximum 3 images allowed"));
  //  }

  //  @Test
  //  void createProduct_WithLargeImage_ShouldReturnBadRequest() throws Exception {
  //    MockMultipartFile productFile =
  //        new MockMultipartFile(
  //            "product",
  //            "",
  //            MediaType.APPLICATION_JSON_VALUE,
  //            objectMapper.writeValueAsBytes(createRequestDto));
  //
  //    doThrow(new MaxUploadSizeExceededException(1024 * 1024))
  //        .when(productService)
  //        .createProduct(any(), any());
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.multipart("/v1/products")
  //                .file(productFile)
  ////                .file(testImage)
  //                .header("Authorization", "Bearer test-token")
  //                .requestAttr("userId", TEST_USER_ID))
  //        .andExpect(status().isBadRequest())
  //        .andExpect(jsonPath("$.message").value("File size exceeds maximum limit"));
  //  }

  //  @Test
  //  void updateProduct_Success() throws Exception {
  //    when(productService.updateProduct(eq(1L), any(Product.class),
  // any())).thenReturn(testProduct);
  //
  //    MockMultipartFile productFile =
  //        new MockMultipartFile(
  //            "product",
  //            "",
  //            MediaType.APPLICATION_JSON_VALUE,
  //            objectMapper.writeValueAsBytes(createRequestDto));
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.multipart("/v1/products/{id}", 1L)
  //                .file(productFile)
  ////                .file(testImage)
  //                .header("Authorization", "Bearer test-token")
  //                .requestAttr("userId", TEST_USER_ID)
  //                .with(
  //                    request -> {
  //                      request.setMethod("PUT");
  //                      return request;
  //                    }))
  //        .andExpect(status().isOk())
  //        .andExpect(jsonPath("$.id").value(testProduct.getId()));
  //  }
  //
  //  @Test
  //  void updateProduct_ProductNotFound_ShouldReturnNotFound() throws Exception {
  //    when(productService.updateProduct(eq(999L), any(Product.class), any()))
  //        .thenThrow(new CustomException(ErrorCode.NOT_FOUND, "Product not found"));
  //
  //    MockMultipartFile productFile =
  //        new MockMultipartFile(
  //            "product",
  //            "",
  //            MediaType.APPLICATION_JSON_VALUE,
  //            objectMapper.writeValueAsBytes(createRequestDto));
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.multipart("/v1/products/{id}", 999L)
  //                .file(productFile)
  //                .header("Authorization", "Bearer test-token")
  //                .requestAttr("userId", TEST_USER_ID)
  //                .with(
  //                    request -> {
  //                      request.setMethod("PUT");
  //                      return request;
  //                    }))
  //        .andExpect(status().isNotFound());
  //  }
  //
  //  @Test
  //  void updateProduct_UnauthorizedSeller_ShouldReturnForbidden() throws Exception {
  //    when(productService.updateProduct(eq(1L), any(Product.class), any()))
  //        .thenThrow(new CustomException(ErrorCode.FORBIDDEN, "Unauthorized seller"));
  //
  //    MockMultipartFile productFile =
  //        new MockMultipartFile(
  //            "product",
  //            "",
  //            MediaType.APPLICATION_JSON_VALUE,
  //            objectMapper.writeValueAsBytes(createRequestDto));
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.multipart("/v1/products/{id}", 1L)
  //                .file(productFile)
  //                .header("Authorization", "Bearer test-token")
  //                .requestAttr("userId", 999L) // Different user ID
  //                .with(
  //                    request -> {
  //                      request.setMethod("PUT");
  //                      return request;
  //                    }))
  //        .andExpect(status().isForbidden());
  //  }
  //
  //  @Test
  //  void deleteProduct_Success() throws Exception {
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.delete("/v1/products/1").requestAttr("userId", TEST_USER_ID))
  //        .andExpect(status().isNoContent());
  //
  //    verify(productService).deleteProduct(1L, TEST_USER_ID);
  //  }
  //
  //  @Test
  //  void deleteProduct_ProductNotFound_ShouldReturnNotFound() throws Exception {
  //    doThrow(new CustomException(ErrorCode.NOT_FOUND, "Product not found"))
  //        .when(productService)
  //        .deleteProduct(999L, TEST_USER_ID);
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.delete("/v1/products/{id}", 999L)
  //                .header("Authorization", "Bearer test-token")
  //                .requestAttr("userId", TEST_USER_ID))
  //        .andExpect(status().isNotFound());
  //  }
  //
  //  @Test
  //  void updateProductStatus_WithInvalidStatus_ShouldReturnBadRequest() throws Exception {
  //    ProductStatusUpdateRequestDto statusUpdate = new ProductStatusUpdateRequestDto(null);
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.patch("/v1/products/{id}/status", 1L)
  //                .contentType(MediaType.APPLICATION_JSON)
  //                .content(objectMapper.writeValueAsBytes(statusUpdate))
  //                .header("Authorization", "Bearer test-token")
  //                .requestAttr("userId", TEST_USER_ID))
  //        .andExpect(status().isBadRequest());
  //  }

  //  @Test
  //  void getProduct_Success() throws Exception {
  //    when(productService.getProduct(1L)).thenReturn(testProduct);
  //
  //    mockMvc
  //        .perform(MockMvcRequestBuilders.get("/v1/products/{id}", 1L))
  //        .andExpect(status().isOk())
  //        .andExpect(jsonPath("$.id").value(testProduct.getId()))
  //        .andExpect(jsonPath("$.title").value(testProduct.getTitle()))
  //        .andExpect(jsonPath("$.status").value(testProduct.getStatus().name()));
  //  }
  //
  //  @Test
  //  void getProduct_NotFound_ShouldReturnNotFound() throws Exception {
  //    when(productService.getProduct(999L))
  //        .thenThrow(new CustomException(ErrorCode.NOT_FOUND, "Product not found"));
  //
  //    mockMvc
  //        .perform(MockMvcRequestBuilders.get("/v1/products/{id}", 999L))
  //        .andExpect(status().isNotFound());
  //  }
  //
  //  @Test
  //  void getSellerProducts_Success() throws Exception {
  //    List<Product> products = Arrays.asList(testProduct);
  //    when(productService.getProductsBySeller(TEST_USER_ID)).thenReturn(products);
  //
  //    mockMvc
  //        .perform(MockMvcRequestBuilders.get("/v1/products/seller/{sellerId}", TEST_USER_ID))
  //        .andExpect(status().isOk())
  //        .andExpect(jsonPath("$[0].id").value(testProduct.getId()))
  //        .andExpect(jsonPath("$[0].title").value(testProduct.getTitle()))
  //        .andExpect(jsonPath("$[0].category").value(testProduct.getCategory()));
  //  }
  //
  //  @Test
  //  void getProductsByCategory_EmptyCategory_ShouldReturnEmptyList() throws Exception {
  //    when(productService.getProductsByCategory("NonExistentCategory"))
  //        .thenReturn(Collections.emptyList());
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.get("/v1/products/category/{category}",
  // "NonExistentCategory"))
  //        .andExpect(status().isOk())
  //        .andExpect(jsonPath("$").isArray())
  //        .andExpect(jsonPath("$").isEmpty());
  //  }
  //
  //  @Test
  //  void getProductsByCategory_InvalidCategory_ShouldReturnBadRequest() throws Exception {
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.get("/v1/products/category")
  //                .contentType(MediaType.APPLICATION_JSON))
  //        .andExpect(status().isBadRequest())
  //        .andExpect(jsonPath("$.message").value("Invalid parameter type for id"))
  //        .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.getErrorCodeValue()));
  //  }

  @Test
  void createProduct_WithoutRequiredFields_ShouldReturnBadRequest() throws Exception {
    ProductDto invalidProductDto =
        ProductDto.builder()
            .price(new Price(100000, CurrencyUnit.TOMAN))
            .category("Electronics")
            .productionYear(2023)
            .brand("Test Brand")
            .build();

    when(productService.createProduct(any(), any()))
        .thenThrow(
            new CustomException(ErrorCode.BAD_REQUEST, "Title and description are required"));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDto))
                .header("Authorization", "Bearer test-token")
                .requestAttr("userId", TEST_USER_ID))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Title and description are required"))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.getErrorCodeValue()));
  }

  @Test
  void createProduct_WithInvalidPrice_ShouldReturnBadRequest() throws Exception {
    ProductDto invalidPriceProductDto =
        ProductDto.builder()
            .title("Test Product")
            .description("Test Description")
            .price(new Price(-100, CurrencyUnit.TOMAN))
            .category("Electronics")
            .productionYear(2023)
            .brand("Test Brand")
            .build();

    when(productService.createProduct(any(), any()))
        .thenThrow(new CustomException(ErrorCode.BAD_REQUEST, "Price must be positive"));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPriceProductDto))
                .header("Authorization", "Bearer test-token")
                .requestAttr("userId", TEST_USER_ID))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Price must be positive"))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.BAD_REQUEST.getErrorCodeValue()));
  }

  //  @Test
  //  void updateProduct_WithNoChanges_ShouldReturnOriginalProduct() throws Exception {
  //    when(productService.updateProduct(eq(1L), any(Product.class),
  // any())).thenReturn(testProduct);
  //
  //    MockMultipartFile productFile =
  //        new MockMultipartFile(
  //            "product",
  //            "",
  //            MediaType.APPLICATION_JSON_VALUE,
  //            objectMapper.writeValueAsBytes(createRequestDto));
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.multipart("/v1/products/{id}", 1L)
  //                .file(productFile)
  //                .header("Authorization", "Bearer test-token")
  //                .requestAttr("userId", TEST_USER_ID)
  //                .with(
  //                    request -> {
  //                      request.setMethod("PUT");
  //                      return request;
  //                    }))
  //        .andExpect(status().isOk())
  //        .andExpect(jsonPath("$.id").value(testProduct.getId()))
  //        .andExpect(jsonPath("$.title").value(testProduct.getTitle()))
  //        .andExpect(jsonPath("$.description").value(testProduct.getDescription()));
  //  }
  //
  //  @Test
  //  void updateProductStatus_WithNullStatus_ShouldReturnBadRequest() throws Exception {
  //    ProductStatusUpdateDto statusUpdate = new ProductStatusUpdateDto(null);
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.patch("/v1/products/1/status")
  //                .contentType(MediaType.APPLICATION_JSON)
  //                .content(objectMapper.writeValueAsBytes(statusUpdate))
  //                .requestAttr("userId", TEST_USER_ID))
  //        .andExpect(status().isBadRequest());
  //  }
  //
  //  @Test
  //  void updateProductStatus_Success() throws Exception {
  //    ProductStatusUpdateDto statusUpdate = new ProductStatusUpdateDto(ProductStatus.RESERVED);
  //    testProduct.setStatus(ProductStatus.RESERVED);
  //
  //    when(productService.updateProductStatus(eq(1L), eq(ProductStatus.RESERVED),
  // eq(TEST_USER_ID)))
  //        .thenReturn(testProduct);
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.patch("/v1/products/1/status")
  //                .contentType(MediaType.APPLICATION_JSON)
  //                .content(objectMapper.writeValueAsBytes(statusUpdate))
  //                .requestAttr("userId", TEST_USER_ID))
  //        .andExpect(status().isOk())
  //        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
  //        .andExpect(jsonPath("$.status").value(ProductStatus.RESERVED.name()));
  //  }
  //
  //  @Test
  //  void updateProductStatus_ToDeletedStatus_ShouldSucceed() throws Exception {
  //    ProductStatusUpdateDto statusUpdate = new ProductStatusUpdateDto(ProductStatus.DELETED);
  //    testProduct.setStatus(ProductStatus.DELETED); // Update test product status
  //
  //    when(productService.updateProductStatus(1L, ProductStatus.DELETED, TEST_USER_ID))
  //            .thenReturn(testProduct);
  //
  //    mockMvc.perform(MockMvcRequestBuilders.patch("/v1/products/1/status")
  //                    .contentType(MediaType.APPLICATION_JSON)
  //                    .content(objectMapper.writeValueAsString(statusUpdate))
  //                    .header("Authorization", "Bearer test-token")
  //                    .requestAttr("userId", TEST_USER_ID))
  //            .andExpect(status().isOk())
  //            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
  //            .andExpect(jsonPath("$.status").value(ProductStatus.DELETED.name()));
  //  }
  //
  //  @Test
  //  void updateProductStatus_WithoutUserIdAttribute_ShouldReturnUnauthorized() throws Exception {
  //    ProductStatusUpdateDto statusUpdate = new ProductStatusUpdateDto(ProductStatus.RESERVED);
  //
  //    mockMvc
  //        .perform(
  //            MockMvcRequestBuilders.patch("/v1/products/1/status")
  //                .contentType(MediaType.APPLICATION_JSON)
  //                .content(objectMapper.writeValueAsBytes(statusUpdate))
  //                .header("Authorization", "Bearer test-token"))
  //        .andExpect(status().isUnauthorized());
  //  }
}
